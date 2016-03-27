package jabsc.classgen;

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
import javassist.bytecode.Bytecode;
import javassist.bytecode.Opcode;

final class StatementVisitor implements Stm.Visitor<Bytecode, Bytecode> {

    private final MethodState methodState;
    private final VisitorState state;
    private final EffExpVisitor effExpVisitor;
    private final PureExpVisitor pureExpVisitor;
    private final TypeVisitor typeVisitor;

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
        this.effExpVisitor = new EffExpVisitor(this.methodState, this.state);
        this.pureExpVisitor = new PureExpVisitor(this.methodState, this.state);
        this.typeVisitor = new TypeVisitor(state::processQType);
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
        p.pureexp_.accept(pureExpVisitor, arg);
        Bytecode subCode = p.stm_.accept(this, ByteCodeUtil.newByteCode(arg));
        ByteCodeUtil.addBranch(arg, Opcode.GOTO, 0);
        return arg;
    }

    @Override
    public Bytecode visit(SReturn p, Bytecode arg) {
        Bytecode bt = p.exp_.accept(expVisitor, ByteCodeUtil.newByteCode(arg));
        arg = ByteCodeUtil.add(bt, arg);
        arg.addOpcode(Opcode.ARETURN);
        return arg;
    }

    @Override
    public Bytecode visit(SAss p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SFieldAss p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SDec p, Bytecode arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(SDecAss p, Bytecode arg) {
        arg = p.exp_.accept(expVisitor, arg);
        int varPos = methodState.addLocalVariable(p.lident_);
        arg.addAstore(varPos);
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
        throw new UnsupportedOperationException();
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
