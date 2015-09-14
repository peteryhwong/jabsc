package jabsc.classgen;

import javassist.bytecode.Opcode;

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
import bnfc.abs.Absyn.PureExp.Visitor;
import javassist.bytecode.Bytecode;

final class PureExpVisitor implements Visitor<Bytecode, Bytecode> {

    private final VisitorState state;
    private final LiteralVisitor literalVisitor;
    
    PureExpVisitor(VisitorState state) {
        this.state = state;
        this.literalVisitor = new LiteralVisitor();
    }
    
    private static Bytecode addOperation(int opcode, Bytecode arg) {
        int index = arg.getSize() - 1;
        arg.addOpcode(opcode); //1
        
        int firstByte = index + 6 >> 8;
        int secondByte = index + 6;
        arg.add(firstByte, secondByte); //3
        
        arg.addIconst(1); //4
        arg.addOpcode(Opcode.GOTO); //5
        
        firstByte = index + 9 >> 8;
        secondByte = index + 9;
        arg.add(firstByte, secondByte); //7
        
        arg.addIconst(0); //8
        return arg;
    }
    
    @Override
    public Bytecode visit(EOr p, Bytecode arg) {
        p.pureexp_1.accept(this, arg);
        p.pureexp_2.accept(this, arg);
        return null;
    }

    @Override
    public Bytecode visit(EAnd p, Bytecode arg) {
        p.pureexp_1.accept(this, arg);
        p.pureexp_2.accept(this, arg);
        return null;
    }

    @Override
    public Bytecode visit(EEq p, Bytecode arg) {
        p.pureexp_1.accept(this, arg);
        p.pureexp_2.accept(this, arg);
        return addOperation(Opcode.IF_ICMPEQ, arg);
    }

    @Override
    public Bytecode visit(ENeq p, Bytecode arg) {
        p.pureexp_1.accept(this, arg);
        p.pureexp_2.accept(this, arg);
        return addOperation(Opcode.IF_ICMPNE, arg);
    }

    @Override
    public Bytecode visit(ELt p, Bytecode arg) {
        p.pureexp_1.accept(this, arg);
        p.pureexp_2.accept(this, arg);
        return addOperation(Opcode.IF_ICMPLT, arg);
    }

    @Override
    public Bytecode visit(ELe p, Bytecode arg) {
        p.pureexp_1.accept(this, arg);
        p.pureexp_2.accept(this, arg);
        return addOperation(Opcode.IF_ICMPLE, arg);
    }

    @Override
    public Bytecode visit(EGt p, Bytecode arg) {
        p.pureexp_1.accept(this, arg);
        p.pureexp_2.accept(this, arg);
        return addOperation(Opcode.IF_ICMPGT, arg);
    }

    @Override
    public Bytecode visit(EGe p, Bytecode arg) {
        p.pureexp_1.accept(this, arg);
        p.pureexp_2.accept(this, arg);
        return addOperation(Opcode.IF_ICMPGE, arg);
    }

    @Override
    public Bytecode visit(EAdd p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(ESub p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(EMul p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(EDiv p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
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
