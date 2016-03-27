package jabsc.classgen;

import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import bnfc.abs.Absyn.AsyncMethCall;
import bnfc.abs.Absyn.Get;
import bnfc.abs.Absyn.New;
import bnfc.abs.Absyn.NewLocal;
import bnfc.abs.Absyn.Spawns;
import bnfc.abs.Absyn.SyncMethCall;
import bnfc.abs.Absyn.ThisAsyncMethCall;
import bnfc.abs.Absyn.ThisSyncMethCall;
import bnfc.abs.Absyn.EffExp.Visitor;
import javassist.bytecode.Bytecode;

final class EffExpVisitor implements Visitor<Bytecode, Bytecode> {

    private final MethodState methodState;
    private final VisitorState state;
    private final TypeVisitor typeVisitor;
    private final PureExpVisitor pureExpVisitor;

    EffExpVisitor(VisitorState state) {
        this(null, state);
    }
    
    EffExpVisitor(MethodState methodState, VisitorState state) {
        this.methodState = methodState;
        this.state = state;
        this.typeVisitor = new TypeVisitor(state::processQType);
        this.pureExpVisitor = new PureExpVisitor(methodState, state);
    }

    @Override
    public Bytecode visit(New p, Bytecode arg) {
        p.listpureexp_.forEach(e -> e.accept(pureExpVisitor, arg));
        
        /*
         * typeVisitor returns type description but does not include the 'L' prefix
         */
        String className = p.type_.accept(typeVisitor, new StringBuilder()).substring(1).toString();
        arg.addNew(className);

        /*
         * duplicate the reference to the new object
         */
        arg.addOpcode(Opcode.DUP);
        arg.addInvokespecial(className, MethodInfo.nameInit, state.getConstructorDescriptor(className));
        return arg;
    }

    @Override
    public Bytecode visit(NewLocal p, Bytecode arg) {
        return visit(new New(p.type_, p.listpureexp_), arg);
    }

    @Override
    public Bytecode visit(SyncMethCall p, Bytecode arg) {
        /*
         * Resolves object reference
         */
        p.pureexp_.accept(pureExpVisitor, arg);
        return null;
    }

    @Override
    public Bytecode visit(ThisSyncMethCall p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(AsyncMethCall p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ThisAsyncMethCall p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(Get p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(Spawns p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

}
