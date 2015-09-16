package jabsc.classgen;

import bnfc.abs.Absyn.Param;
import bnfc.abs.Absyn.Type;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
interface DescriptorCreator extends Function<VisitorState, BiFunction<Type, List<Param>, String>> {

}
