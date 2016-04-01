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

    PureExpVisitor(MethodState methodState, VisitorState state) {
        this.methodState = methodState;
        this.state = state;
        this.literalVisitor = new LiteralVisitor();
        this.currentModule = state.getCurrentModule();
    }

    /**
     * Evaluate a boolean statement (conjunction or disjunction)
     * 
     * @param lhs
     * @param rhs
     * @param branchOp boolean operation (conjunction or disjunction)
     * @param defaultValue
     * @param branchValue
     * @param arg
     * @return
     */
    private Bytecode booleanOp(PureExp lhs, PureExp rhs, int branchOp, int defaultValue,
                    int branchValue, Bytecode arg) {
        // Evaluate lhs
        arg = lhs.accept(this, arg);
        arg = ByteCodeUtil.toBooleanValue(arg);

        // branch to endOffset if operation is true
        arg.addOpcode(branchOp);
        // 2 byte place holders for offset
        int offsetIndexLhs = arg.currentPc();
        arg.add(-1, -1);

        // Evaluate rhs
        arg = rhs.accept(this, arg);
        arg = ByteCodeUtil.toBooleanValue(arg);

        // branch to endOffset if operation is true
        arg.addOpcode(branchOp);
        // 2 byte place holders for offset
        int offsetIndexRhs = arg.currentPc();
        arg.add(-1, -1);

        // fall through value
        arg.addOpcode(defaultValue);
        arg.add(Opcode.GOTO);
        // 2 byte place holders for offset
        int offsetIndexEnd = arg.currentPc();
        arg.add(-1, -1);

        // branch value
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

    private Bytecode equals(PureExp lhs, PureExp rhs, Bytecode arg) {
        arg = lhs.accept(this, arg);
        arg = rhs.accept(this, arg);
        arg.addInvokestatic("java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z");
        return ByteCodeUtil.toBoolean(arg);
    }
    
    private Bytecode negEquals(Bytecode arg) {
        /*
         * branch to endOffset if 0 (false)
         * Opcode.ICONST_1 if exp
         * Opcode.ICONST_0 if ! exp
         */
        // branch to endOffset if operation is false
        arg.addOpcode(Opcode.IFNE);
        // 2 byte place holders for offset
        int offsetIndex = arg.currentPc();
        arg.add(-1, -1);

        // fall through value
        arg.addOpcode(Opcode.ICONST_1);
        arg.add(Opcode.GOTO);
        // 2 byte place holders for offset
        int offsetIndexEnd = arg.currentPc();
        arg.add(-1, -1);

        // branch value
        int endOffset = arg.currentPc();
        arg.addOpcode(Opcode.ICONST_0);

        // Update offsets
        arg.write16bit(offsetIndex, endOffset - offsetIndex + 1);
        arg.write16bit(offsetIndexEnd, arg.currentPc() - offsetIndexEnd + 1);
        return ByteCodeUtil.toBoolean(arg);
    }
    
    @Override
    public Bytecode visit(EEq p, Bytecode arg) {
        return equals(p.pureexp_1, p.pureexp_2, arg);
    }

    @Override
    public Bytecode visit(ENeq p, Bytecode arg) {
        /*
         * branch to endOffset if 0 (false)
         * Opcode.ICONST_1 if exp
         * Opcode.ICONST_0 if ! exp
         */
        // Evaluate expression against equals
        arg = equals(p.pureexp_1, p.pureexp_2, arg);
        arg = ByteCodeUtil.toBooleanValue(arg);
        return negEquals(arg);
    }
    
    private Bytecode longComparison(PureExp lhs, PureExp rhs, int cmpOp, Bytecode arg) {
        // Evaluate lhs
        arg = lhs.accept(this, arg);
        arg = ByteCodeUtil.toLongValue(arg);

        // Evaluate rhs
        arg = rhs.accept(this, arg);
        arg = ByteCodeUtil.toLongValue(arg);
        
        // Compare two values
        arg.addOpcode(Opcode.LCMP);

        // branch to endOffset if operation is true
        arg.addOpcode(cmpOp);
        // 2 byte place holders for offset
        int offsetIndex = arg.currentPc();
        arg.add(-1, -1);

        // fall through location
        arg.addOpcode(Opcode.ICONST_0);
        arg.add(Opcode.GOTO);
        // 2 byte place holders for offset
        int offsetIndexEnd = arg.currentPc();
        arg.add(-1, -1);

        // branch location
        int endOffset = arg.currentPc();
        arg.addOpcode(Opcode.ICONST_1);

        // Update offsets
        arg.write16bit(offsetIndex, endOffset - offsetIndex + 1);
        arg.write16bit(offsetIndexEnd, arg.currentPc() - offsetIndexEnd + 1);
        return ByteCodeUtil.toBoolean(arg);
    }

    @Override
    public Bytecode visit(ELt p, Bytecode arg) {
        return longComparison(p.pureexp_1, p.pureexp_2, Opcode.IFLT, arg);
    }

    @Override
    public Bytecode visit(ELe p, Bytecode arg) {
        return longComparison(p.pureexp_1, p.pureexp_2, Opcode.IFLE, arg);
    }

    @Override
    public Bytecode visit(EGt p, Bytecode arg) {
        return longComparison(p.pureexp_1, p.pureexp_2, Opcode.IFGT, arg);
    }

    @Override
    public Bytecode visit(EGe p, Bytecode arg) {
        return longComparison(p.pureexp_1, p.pureexp_2, Opcode.IFGE, arg);
    }

    private Bytecode arithmetic(PureExp lhs, PureExp rhs, int operation, Bytecode arg) {
        arg = lhs.accept(this, arg);
        arg = ByteCodeUtil.toLongValue(arg);
        arg = rhs.accept(this, arg);
        arg = ByteCodeUtil.toLongValue(arg);
        arg.addOpcode(operation);
        return ByteCodeUtil.toLong(arg);
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
        return arithmetic(p.pureexp_1, p.pureexp_2, Opcode.LREM, arg);
    }

    @Override
    public Bytecode visit(ELogNeg p, Bytecode arg) {
        arg = p.pureexp_.accept(this, arg);
        arg = ByteCodeUtil.toBooleanValue(arg);
        return negEquals(arg);
    }

    @Override
    public Bytecode visit(EIntNeg p, Bytecode arg) {
        arg = p.pureexp_.accept(this, arg);
        arg = ByteCodeUtil.toLongValue(arg);
        arg.addOpcode(Opcode.LNEG);
        return ByteCodeUtil.toLong(arg);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(ENaryFunCall p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(ENaryQualFunCall p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(EVar p, Bytecode arg) {
        arg.addAload(methodState.getLocalVariable(p.lident_));
        return arg;
    }

    @Override
    public Bytecode visit(EThis p, Bytecode arg) {
        //load this
        arg.addAload(0);
        String className = arg.getConstPool().getClassName();
        String fullyQualifiedName = className + "." + p.lident_;
        String type = currentModule.getNameToSignature().get(fullyQualifiedName);
        arg.addGetfield(className, p.lident_, type);
        return arg;
    }

    @Override
    public Bytecode visit(EQualVar p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(ESinglConstr p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(EParamConstr p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(ELit p, Bytecode arg) {
        return p.literal_.accept(literalVisitor, arg);
    }

    @Override
    public Bytecode visit(Let p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(If p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(Case p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

}
