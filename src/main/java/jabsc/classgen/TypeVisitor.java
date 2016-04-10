package jabsc.classgen;

import bnfc.abs.Absyn.QType;
import bnfc.abs.Absyn.TGen;
import bnfc.abs.Absyn.TSimple;
import bnfc.abs.Absyn.TUnderscore;
import bnfc.abs.Absyn.Type;

import java.util.function.Function;

final class TypeVisitor implements Type.Visitor<StringBuilder, StringBuilder> {

    private final Function<QType, String> processQType;

    TypeVisitor(Function<QType, String> processQType) {
        this.processQType = processQType
                .andThen(s -> s.replace('.', '/'))
                .andThen(StateUtil.ABS_TO_JDK);
    }

    @Override
    public StringBuilder visit(TUnderscore p, StringBuilder arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder visit(TSimple p, StringBuilder arg) {
        return arg.append(processQType.apply(p.qtype_));
    }

    @Override
    public StringBuilder visit(TGen p, StringBuilder arg) {
        return arg.append(processQType.apply(p.qtype_));     
    }

}
