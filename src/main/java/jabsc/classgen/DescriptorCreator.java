package jabsc.classgen;

import bnfc.abs.Absyn.Param;
import bnfc.abs.Absyn.QType;
import bnfc.abs.Absyn.Type;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

final class DescriptorCreator implements Function<Function<QType, String>, BiFunction<Type, List<Param>, String>> {

    @Override
    public BiFunction<Type, List<Param>, String> apply(Function<QType, String> processQType) {
        return (returnType, params) -> createDescriptor(returnType, params, processQType);
    }
    
    private static String createDescriptor(Type returnType, List<Param> params, Function<QType, String> processQType) {
        TypeVisitor typeVisitor = new TypeVisitor(processQType);
        StringBuilder descriptor = new StringBuilder().append('(');

        if (!params.isEmpty()) {
            params.forEach(param -> param.accept((par, sb) -> {
                par.type_.accept(typeVisitor, sb);
                sb.append(';');
                return null;
            }, descriptor));
        }

        descriptor.append(')');

        /*
         * null is the same as void
         */
        if (returnType == null) {
            descriptor.append('V');
        } else {
            StringBuilder retype = new StringBuilder();
            returnType.accept(typeVisitor, retype);
            if (retype.charAt(0) != 'V') {
                retype.append(';');
            }
            descriptor.append(retype);
        }

        return descriptor.toString();
    }

}
