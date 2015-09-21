package jabsc.classgen;

import bnfc.abs.Absyn.LFalse;
import bnfc.abs.Absyn.LInt;
import bnfc.abs.Absyn.LNull;
import bnfc.abs.Absyn.LStr;
import bnfc.abs.Absyn.LThis;
import bnfc.abs.Absyn.LThisDC;
import bnfc.abs.Absyn.LTrue;

import bnfc.abs.Absyn.Literal.Visitor;
import javassist.bytecode.Bytecode;

final class LiteralVisitor implements Visitor<Bytecode, Bytecode> {

    @Override
    public Bytecode visit(LNull p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(LThis p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(LThisDC p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(LStr p, Bytecode arg) {
        int index = arg.getConstPool().addStringInfo(p.string_);
        arg.addLdc(index);
        return arg;
    }

    @Override
    public Bytecode visit(LInt p, Bytecode arg) {
        arg.addLconst(p.integer_.longValue());
        arg.addInvokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        return arg;
    }

    @Override
    public Bytecode visit(LFalse p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bytecode visit(LTrue p, Bytecode arg) {
        // TODO Auto-generated method stub
        return null;
    }

}
