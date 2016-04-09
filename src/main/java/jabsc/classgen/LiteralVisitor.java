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
import javassist.bytecode.Opcode;

final class LiteralVisitor implements Visitor<Bytecode, Bytecode> {

    @Override
    public Bytecode visit(LNull p, Bytecode arg) {
        arg.add(Opcode.ACONST_NULL);
        return arg;
    }

    @Override
    public Bytecode visit(LThis p, Bytecode arg) {
        arg.addIload(0);
        return arg;
    }

    @Override
    public Bytecode visit(LThisDC p, Bytecode arg) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytecode visit(LStr p, Bytecode arg) {
        int index = arg.getConstPool().addStringInfo(p.string_);
        arg.addLdc(index);
        return arg;
    }

    @Override
    public Bytecode visit(LInt p, Bytecode arg) {
        arg.addIconst(p.integer_.intValue());
        return ByteCodeUtil.toInteger(arg);
    }

    @Override
    public Bytecode visit(LFalse p, Bytecode arg) {
        arg.addGetstatic("java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
        return arg;
    }

    @Override
    public Bytecode visit(LTrue p, Bytecode arg) {
        arg.addGetstatic("java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
        return arg;
    }

}
