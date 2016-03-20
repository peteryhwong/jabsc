package jabsc.classgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import bnfc.abs.Absyn.Case;
import bnfc.abs.Absyn.EAdd;
import bnfc.abs.Absyn.EAnd;
import bnfc.abs.Absyn.EDiv;
import bnfc.abs.Absyn.EEq;
import bnfc.abs.Absyn.EFunCall;
import bnfc.abs.Absyn.EGe;
import bnfc.abs.Absyn.EGt;
import bnfc.abs.Absyn.EIntNeg;
import bnfc.abs.Absyn.ELe;
import bnfc.abs.Absyn.ELit;
import bnfc.abs.Absyn.ELogNeg;
import bnfc.abs.Absyn.ELt;
import bnfc.abs.Absyn.EMod;
import bnfc.abs.Absyn.EMul;
import bnfc.abs.Absyn.ENaryFunCall;
import bnfc.abs.Absyn.ENaryQualFunCall;
import bnfc.abs.Absyn.ENeq;
import bnfc.abs.Absyn.EOr;
import bnfc.abs.Absyn.EParamConstr;
import bnfc.abs.Absyn.EQualFunCall;
import bnfc.abs.Absyn.EQualVar;
import bnfc.abs.Absyn.ESinglConstr;
import bnfc.abs.Absyn.ESub;
import bnfc.abs.Absyn.EThis;
import bnfc.abs.Absyn.EVar;
import bnfc.abs.Absyn.If;
import bnfc.abs.Absyn.Let;
import bnfc.abs.Absyn.PureExp;
import bnfc.abs.Absyn.PureExp.Visitor;
import jabsc.classgen.VisitorState.ModuleInfo;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Opcode;

final class PureExpVisitor implements Visitor<Bytecode, Bytecode> {

    private static final Map<String, String> FUNCTIONALS = new HashMap<>();

    static {
        FUNCTIONALS.put("toString", "(Ljava/lang/Object;)Ljava/lang/String;");
    }

    private final MethodState methodState;
    private final VisitorState state;
    private final LiteralVisitor literalVisitor;
    private final ModuleInfo currentModule;

    PureExpVisitor(VisitorState state) {
        this(null, state);
    }
        
    PureExpVisitor(MethodState methodState, VisitorState state) {
        this.methodState = methodState;
        this.state = state;
        this.literalVisitor = new LiteralVisitor();
        this.currentModule = state.getCurrentModule();
    }

    private Bytecode arithmetic(PureExp lhs, PureExp rhs, int operation, Bytecode arg) {
        Bytecode sub = lhs.accept(this, ByteCodeUtil.newByteCode(arg));
        arg = ByteCodeUtil.toLongValue(ByteCodeUtil.add(sub, arg));
        sub = rhs.accept(this, ByteCodeUtil.newByteCode(arg));
        arg = ByteCodeUtil.toLongValue(ByteCodeUtil.add(sub, arg));
        arg.addOpcode(operation);
        return ByteCodeUtil.toLong(arg);
    }
    
    /**
     * 
     * @param lhs
     * @param rhs
     * @param branchOp
     * @param defaultValue
     * @param branchValue
     * @param arg
     * @return
     */
    private Bytecode booleanOp(PureExp lhs, PureExp rhs, int branchOp, int defaultValue,
                    int branchValue, Bytecode arg) {
        // Evaluate lhs
        Bytecode sub = lhs.accept(this, ByteCodeUtil.newByteCode(arg));
        arg = ByteCodeUtil.toBooleanValue(ByteCodeUtil.add(sub, arg));

        // branch to endOffset if operation is true
        arg.addOpcode(branchOp);
        // 2 byte place holders for offset
        int offsetIndexLhs = arg.currentPc();
        arg.add(-1, -1);

        // Evaluate rhs
        sub = rhs.accept(this, ByteCodeUtil.newByteCode(arg));
        arg = ByteCodeUtil.toBooleanValue(ByteCodeUtil.add(sub, arg));

        // branch to endOffset if operation is true
        arg.addOpcode(branchOp);
        // 2 byte place holders for offset
        int offsetIndexRhs = arg.currentPc();
        arg.add(-1, -1);

        // default location
        arg.addOpcode(defaultValue);
        arg.add(Opcode.GOTO);
        // 2 byte place holders for offset
        int offsetIndexEnd = arg.currentPc();
        arg.add(-1, -1);

        // branch location
        int endOffset = arg.currentPc();
        arg.addOpcode(branchValue);

        // Update offsets
        arg.write16bit(offsetIndexLhs, endOffset - offsetIndexLhs + 1);
        arg.write16bit(offsetIndexRhs, endOffset - offsetIndexRhs + 1);
        arg.write16bit(offsetIndexEnd, arg.currentPc() - offsetIndexEnd + 1);

        return ByteCodeUtil.toBoolean(arg);
    }
    
    @Override
    public Bytecode visit(EOr p, Bytecode arg) {
        /*
         * branch to endOffset if 1 (true)
         * Opcode.ICONST_0 if ! (lhs || rhs)
         * Opcode.ICONST_1 if lhs || rhs
         */
        return booleanOp(p.pureexp_1, p.pureexp_2, Opcode.IFNE, Opcode.ICONST_0, Opcode.ICONST_1, arg);
    }

    @Override
    public Bytecode visit(EAnd p, Bytecode arg) {
        /*
         * branch to endOffset if 0 (false)
         * Opcode.ICONST_1 if lhs && rhs
         * Opcode.ICONST_0 if ! (lhs && rhs)
         */
        return booleanOp(p.pureexp_1, p.pureexp_2, Opcode.IFEQ, Opcode.ICONST_1, Opcode.ICONST_0, arg);
    }

    @Override
    public Bytecode visit(EEq p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ENeq p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ELt p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ELe p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(EGt p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(EGe p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(EAdd p, Bytecode arg) {
        return arithmetic(p.pureexp_1, p.pureexp_2, Opcode.LADD, arg);
    }

    @Override
    public Bytecode visit(ESub p, Bytecode arg) {
        return arithmetic(p.pureexp_1, p.pureexp_2, Opcode.LSUB, arg);
    }

    @Override
    public Bytecode visit(EMul p, Bytecode arg) {
        return arithmetic(p.pureexp_1, p.pureexp_2, Opcode.LMUL, arg);
    }

    @Override
    public Bytecode visit(EDiv p, Bytecode arg) {
        return arithmetic(p.pureexp_1, p.pureexp_2, Opcode.LDIV, arg);
    }

    @Override
    public Bytecode visit(EMod p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ELogNeg p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(EIntNeg p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(EFunCall p, Bytecode arg) {
        String dotPrefix = "." + p.lident_;
        Optional<String> fullImport =
            currentModule.getImports().stream().filter(s -> s.endsWith(dotPrefix)).findFirst();
        p.listpureexp_.forEach(e -> e.accept(this, arg));
        if (fullImport.isPresent()) {
            String qualified = fullImport.get();
            String descriptor = state.getDescriptor(qualified);
            String module = qualified.substring(0, qualified.length() - dotPrefix.length());
            String function = state.getModuleInfos(module).getFunctionClassName();
            String className = (module + '.' + function).replace('.', '/');
            arg.addInvokestatic(className, p.lident_, descriptor);
        } else {
            String descriptor = FUNCTIONALS.get(p.lident_);
            arg.addInvokestatic(StateUtil.FUNCTIONAL, p.lident_, descriptor);
        }
        return arg;
    }

    @Override
    public Bytecode visit(EQualFunCall p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ENaryFunCall p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ENaryQualFunCall p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(EVar p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(EThis p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(EQualVar p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ESinglConstr p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(EParamConstr p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ELit p, Bytecode arg) {
        return p.literal_.accept(literalVisitor, arg);
    }

    @Override
    public Bytecode visit(Let p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(If p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(Case p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

}
