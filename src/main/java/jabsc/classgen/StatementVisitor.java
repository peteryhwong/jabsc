package jabsc.classgen;

import java.util.UUID;

import bnfc.abs.Absyn.Exp;
import bnfc.abs.Absyn.ExpE;
import bnfc.abs.Absyn.ExpP;
import bnfc.abs.Absyn.SAss;
import bnfc.abs.Absyn.SAssert;
import bnfc.abs.Absyn.SAwait;
import bnfc.abs.Absyn.SBlock;
import bnfc.abs.Absyn.SDec;
import bnfc.abs.Absyn.SDecAss;
import bnfc.abs.Absyn.SExp;
import bnfc.abs.Absyn.SFieldAss;
import bnfc.abs.Absyn.SIf;
import bnfc.abs.Absyn.SIfElse;
import bnfc.abs.Absyn.SPrint;
import bnfc.abs.Absyn.SReturn;
import bnfc.abs.Absyn.SSkip;
import bnfc.abs.Absyn.SSuspend;
import bnfc.abs.Absyn.SThrow;
import bnfc.abs.Absyn.STryCatchFinally;
import bnfc.abs.Absyn.SWhile;
import bnfc.abs.Absyn.Stm;
import jabsc.classgen.BootstrapMethodManager.InterfaceCall;
import jabsc.classgen.VisitorState.ModuleInfo;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Opcode;

final class StatementVisitor implements Stm.Visitor<Bytecode, Bytecode> {

    private final MethodState methodState;
    private final VisitorState state;
    private final EffExpVisitor effExpVisitor;
    private final PureExpVisitor pureExpVisitor;
    private final GuardVisitor guardVisitor;
    private final GuardNameVisitor guardNameVisitor;
    private final GuardTypeVisitor guardTypeVisitor;
    private final TypeVisitor typeVisitor;
    private final ModuleInfo currentModule;

    private final Exp.Visitor<Bytecode, Bytecode> expVisitor =
        new Exp.Visitor<Bytecode, Bytecode>() {

            @Override
            public Bytecode visit(ExpP p, Bytecode arg) {
                return p.pureexp_.accept(pureExpVisitor, arg);
            }

            @Override
            public Bytecode visit(ExpE p, Bytecode arg) {
                return p.effexp_.accept(effExpVisitor, arg);
            }

        };
        
    StatementVisitor(MethodState methodState, VisitorState state) {
        this.methodState = methodState;
        this.state = state;
        this.currentModule = this.state.getCurrentModule();
        this.effExpVisitor = new EffExpVisitor(this.methodState, this.state);
        this.pureExpVisitor = new PureExpVisitor(this.methodState, this.state);
        this.typeVisitor = new TypeVisitor(this.state::processQType);
        this.guardVisitor = new GuardVisitor(this.methodState, this.state);
        this.guardTypeVisitor = new GuardTypeVisitor(this.methodState, this.currentModule);
        this.guardNameVisitor = new GuardNameVisitor();
    }

    @Override
    public Bytecode visit(SExp p, Bytecode arg) {
        return p.exp_.accept(expVisitor, arg);
    }

    @Override
    public Bytecode visit(SBlock p, Bytecode arg) {
        p.liststm_.forEach(stm -> stm.accept(this, arg));
        return arg;
    }

    @Override
    public Bytecode visit(SWhile p, Bytecode arg) {        
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SReturn p, Bytecode arg) {
        arg = p.exp_.accept(expVisitor, arg);
        arg.addOpcode(Opcode.ARETURN);
        return arg;
    }

    @Override
    public Bytecode visit(SAss p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SFieldAss p, Bytecode arg) {
        arg.addAload(0);
        arg = p.exp_.accept(expVisitor, arg);
        String className = arg.getConstPool().getClassName();
        String fullyQualifiedName = className + "." + p.lident_;
        String type = currentModule.getNameToSignature().get(fullyQualifiedName);
        arg.addPutfield(className, p.lident_, type);
        return arg;
    }

    @Override
    public Bytecode visit(SDec p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SDecAss p, Bytecode arg) {
        arg = p.exp_.accept(expVisitor, arg);
        String type = p.type_.accept(typeVisitor, new StringBuilder()).toString();
        arg.addAstore(methodState.addLocalVariable(p.lident_, type));
        return arg;
    }

    @Override
    public Bytecode visit(SIf p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SIfElse p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SSuspend p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SSkip p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SAssert p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SAwait p, Bytecode arg) {
        arg = p.guard_.accept(guardVisitor, arg);
        String type = p.guard_.accept(guardTypeVisitor, new StringBuilder()).toString();

        if (! "Labs/api/Response;".equals(type)) {
            throw new IllegalArgumentException();
        }
        
        /*
         * create a Supplier lambda
         */
        ConstPool constPool = arg.getConstPool();
        BootstrapMethodManager counter = methodState.getCounter();
        InterfaceCall interfaceCall = new InterfaceCall("abs/api/Response", "getValue", "()Ljava/lang/Boolean;", 1);
        int bootstrap = counter.addBootstrapMethod(constPool, interfaceCall, BootstrapMethodManager.SUPPLIER);
        
        /*
         * e.g. InvokeDynamic #1:get:(Labs/api/Response;)Ljava/util/function/Supplier;
         */
        arg.addInvokedynamic(bootstrap, "get", "(Labs/api/Response;)Ljava/util/function/Supplier;");
        
        /*
         * Save to local variable
         */
        String supplierName = "msg" + UUID.randomUUID().toString();
        arg.addAstore(methodState.addLocalVariable(supplierName, "Ljava/util/function/Supplier;"));
        
        /*
         * await:(Ljava/lang/Object;Ljava/util/function/Supplier;)LResponse;
         */
        arg.addAload(0);
        arg.addAload(0);
        arg.addAload(methodState.getLocalVariable(supplierName));
        arg.addInvokevirtual(constPool.getClassName(), "await", "(Ljava/lang/Object;Ljava/util/function/Supplier;)Labs/api/Response;");
        
        return arg;
    }

    @Override
    public Bytecode visit(SThrow p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(STryCatchFinally p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SPrint p, Bytecode arg) {
        p.pureexp_.accept(pureExpVisitor, arg);
        arg.addInvokestatic(StateUtil.FUNCTIONAL, "println", "(Ljava/lang/Object;)V");
        return arg;
    }

}
