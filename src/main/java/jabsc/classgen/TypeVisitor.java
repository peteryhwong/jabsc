package jabsc.classgen;

import bnfc.abs.Absyn.TGen;
import bnfc.abs.Absyn.TSimple;
import bnfc.abs.Absyn.TUnderscore;
import bnfc.abs.Absyn.Type;

final class TypeVisitor implements Type.Visitor<StringBuilder, StringBuilder> {

    private final VisitorState state;

    TypeVisitor(VisitorState state) {
        this.state = state;
    }

    @Override
    public StringBuilder visit(TUnderscore p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(TSimple p, StringBuilder arg) {
        arg.append(StateUtil.ABS_TO_JDK.apply(state.processQType(p.qtype_).replace('.', '/')));
        return arg;
    }

    @Override
    public StringBuilder visit(TGen p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

}