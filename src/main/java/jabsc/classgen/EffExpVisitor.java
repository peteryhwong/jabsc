package jabsc.classgen;

import bnfc.abs.Absyn.AsyncMethCall;
import bnfc.abs.Absyn.EffExp.Visitor;
import bnfc.abs.Absyn.Get;
import bnfc.abs.Absyn.New;
import bnfc.abs.Absyn.NewLocal;
import bnfc.abs.Absyn.Spawns;
import bnfc.abs.Absyn.SyncMethCall;
import bnfc.abs.Absyn.ThisAsyncMethCall;
import bnfc.abs.Absyn.ThisSyncMethCall;
import javassist.bytecode.Bytecode;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

final class EffExpVisitor implements Visitor<Bytecode, Bytecode> {

    private final MethodState methodState;
    private final VisitorState state;
    private final TypeVisitor typeVisitor;
    private final PureExpVisitor pureExpVisitor;
    private final PureExpTypeVisitor pureExpTypeVisitor;

    EffExpVisitor(MethodState methodState, VisitorState state) {
        this.methodState = methodState;
        this.state = state;
        this.typeVisitor = new TypeVisitor(state::processQType);
        this.pureExpVisitor = new PureExpVisitor(methodState, state);
        this.pureExpTypeVisitor = new PureExpTypeVisitor(methodState, state.getCurrentModule());
    }

    @Override
    public Bytecode visit(New p, Bytecode arg) {
        /*
         * typeVisitor returns type description but does not include the 'L' prefix
         */
        String className = p.type_.accept(typeVisitor, new StringBuilder()).substring(1).toString();
        arg.addNew(className);

        /*
         * duplicate the reference to the new object
         */
        arg.addOpcode(Opcode.DUP);
        
        /*
         * Evaluate arguments
         */
        p.listpureexp_.forEach(e -> e.accept(pureExpVisitor, arg));
        arg.addInvokespecial(className, MethodInfo.nameInit, state.getConstructorDescriptor(className));
        return arg;
    }

    @Override
    public Bytecode visit(NewLocal p, Bytecode arg) {
        return visit(new New(p.type_, p.listpureexp_), arg);
    }

    @Override
    public Bytecode visit(SyncMethCall p, Bytecode arg) {
        p.pureexp_.accept(pureExpVisitor, arg);
        String method = p.lident_;
        if (StateUtil.LITERAL_GET.equals(method)) {
            throw new UnsupportedOperationException();
        } else {
            p.listpureexp_.forEach(pe -> pe.accept(pureExpVisitor, arg));
            String className = p.pureexp_.accept(pureExpTypeVisitor, new StringBuilder()).substring(1).toString();
            String fullyQualifiedName = (className + "." + method).replaceAll("/", ".");
            String type = state.getCurrentModule().getNameToSignature().get(fullyQualifiedName);
            arg.addInvokeinterface(className, method, type, p.listpureexp_.size() + 1);
        }
        return arg;
    }

    @Override
    public Bytecode visit(ThisSyncMethCall p, Bytecode arg) {
        arg.addAload(0);
        p.listpureexp_.forEach(pe -> pe.accept(pureExpVisitor, arg));
        String className = methodState.getClassName();
        String method = p.lident_;
        String fullyQualifiedName = (className + "." + method).replaceAll("/", ".");
        String type = state.getCurrentModule().getNameToSignature().get(fullyQualifiedName);
        arg.addInvokevirtual(className, method, type);
        return arg;
    }

    @Override
    public Bytecode visit(AsyncMethCall p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(ThisAsyncMethCall p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(Get p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(Spawns p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

}
