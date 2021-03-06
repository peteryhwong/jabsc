package jabsc.classgen;

import bnfc.abs.AbstractVisitor;
import bnfc.abs.Absyn.ClassBody;
import bnfc.abs.Absyn.ClassDecl;
import bnfc.abs.Absyn.ClassImplements;
import bnfc.abs.Absyn.ClassParamDecl;
import bnfc.abs.Absyn.ClassParamImplements;
import bnfc.abs.Absyn.DataDecl;
import bnfc.abs.Absyn.DataParDecl;
import bnfc.abs.Absyn.ExceptionDecl;
import bnfc.abs.Absyn.ExtendsDecl;
import bnfc.abs.Absyn.FieldAssignClassBody;
import bnfc.abs.Absyn.FieldClassBody;
import bnfc.abs.Absyn.FunDecl;
import bnfc.abs.Absyn.FunParDecl;
import bnfc.abs.Absyn.InterfDecl;
import bnfc.abs.Absyn.MethClassBody;
import bnfc.abs.Absyn.MethSig;
import bnfc.abs.Absyn.MethSignat;
import bnfc.abs.Absyn.Param;
import bnfc.abs.Absyn.Type;
import bnfc.abs.Absyn.TypeDecl;
import bnfc.abs.Absyn.TypeParDecl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

final class DescriptorVisitor extends AbstractVisitor<Map<String, String>, Map<String, String>> {

    private static final class MethodBodyVisitor implements
        ClassBody.Visitor<Map<String, String>, Map<String, String>>,
        MethSignat.Visitor<Map<String, String>, Map<String, String>> {

        private final Function<String, String> fullyQualify;
        private final BiFunction<Type, List<Param>, String> creator;

        private MethodBodyVisitor(Function<String, String> fullyQualify,
            BiFunction<Type, List<Param>, String> creator) {
            this.fullyQualify = fullyQualify;
            this.creator = creator;
        }

        @Override
        public Map<String, String> visit(FieldClassBody p, Map<String, String> arg) {
            String name = fullyQualify.apply(p.lident_);
            String descriptor = creator.apply(p.type_, Collections.emptyList()).substring(2);
            arg.put(name, descriptor);
            return arg;
        }

        @Override
        public Map<String, String> visit(FieldAssignClassBody p, Map<String, String> arg) {
            String name = fullyQualify.apply(p.lident_);
            String descriptor = creator.apply(p.type_, Collections.emptyList()).substring(2);
            arg.put(name, descriptor);
            return arg;
        }

        @Override
        public Map<String, String> visit(MethClassBody p, Map<String, String> arg) {
            String name = fullyQualify.apply(p.lident_);
            String descriptor = creator.apply(p.type_, p.listparam_);
            arg.put(name, descriptor);
            return arg;
        }

        @Override
        public Map<String, String> visit(MethSig p, Map<String, String> arg) {
            String name = fullyQualify.apply(p.lident_);
            String descriptor = creator.apply(p.type_, p.listparam_);
            arg.put(name, descriptor);
            return arg;
        }

    }

    private final Function<String, String> className;
    private final StringBuilder moduleNamePrefix;
    private final BiFunction<Type, List<Param>, String> creator;

    DescriptorVisitor(String moduleName, Function<String, String> className,
        BiFunction<Type, List<Param>, String> creator) {
        this.moduleNamePrefix = new StringBuilder(moduleName).append('.');
        this.className = className;
        this.creator = creator;
    }

    @Override
    public Map<String, String> visit(TypeDecl p, Map<String, String> arg) {
        return arg;
    }

    @Override
    public Map<String, String> visit(TypeParDecl p, Map<String, String> arg) {
        return arg;
    }

    @Override
    public Map<String, String> visit(ExceptionDecl p, Map<String, String> arg) {
        return arg;
    }

    @Override
    public Map<String, String> visit(DataDecl p, Map<String, String> arg) {
        return arg;
    }

    @Override
    public Map<String, String> visit(DataParDecl p, Map<String, String> arg) {
        return arg;
    }

    @Override
    public Map<String, String> visit(FunDecl p, Map<String, String> arg) {
        return arg;
    }

    @Override
    public Map<String, String> visit(FunParDecl p, Map<String, String> arg) {
        return arg;
    }

    @Override
    public Map<String, String> visit(InterfDecl p, Map<String, String> arg) {
        return processInterface(p.uident_, p.listmethsignat_, arg);
    }

    @Override
    public Map<String, String> visit(ExtendsDecl p, Map<String, String> arg) {
        return processInterface(p.uident_, p.listmethsignat_, arg);
    }

    @Override
    public Map<String, String> visit(ClassDecl p, Map<String, String> arg) {
        return processClass(p.uident_, 
                        Collections.emptyList(), 
                        Stream.concat(p.listclassbody_1.stream(), p.listclassbody_2.stream()), 
                        arg);
    }

    @Override
    public Map<String, String> visit(ClassParamDecl p, Map<String, String> arg) {
        return processClass(p.uident_, 
                        p.listparam_, 
                        Stream.concat(p.listclassbody_1.stream(), p.listclassbody_2.stream()), 
                        arg);
    }

    @Override
    public Map<String, String> visit(ClassImplements p, Map<String, String> arg) {
        return processClass(p.uident_, 
                        Collections.emptyList(), 
                        Stream.concat(p.listclassbody_1.stream(), p.listclassbody_2.stream()), 
                        arg);
    }

    @Override
    public Map<String, String> visit(ClassParamImplements p, Map<String, String> arg) {
        return processClass(p.uident_, 
                        p.listparam_, 
                        Stream.concat(p.listclassbody_1.stream(), p.listclassbody_2.stream()), 
                        arg);
    }

    private Map<String, String> processInterface(String interfaceName, List<MethSignat> methods,
        Map<String, String> map) {
        StringBuilder methodPrefix =
            new StringBuilder(getFullyQualifiedName(interfaceName)).append('.');
        MethodBodyVisitor visitor =
            new MethodBodyVisitor(name -> getFullyQualifiedName(methodPrefix, name), creator);
        methods.forEach(m -> m.accept(visitor, map));
        return map;
    }

    private Map<String, String> processClass(String name, List<Param> params,
        Stream<ClassBody> bodies, Map<String, String> map) {
        String constructorName = className.apply(name);
        String fullQualifiedClassName = getFullyQualifiedName(constructorName);
        String descriptor = params.isEmpty() ? "()V" : creator.apply(null, params);
        map.put(new StringBuilder(fullQualifiedClassName).append('.').append(constructorName).toString(), descriptor);

        StringBuilder methodPrefix = new StringBuilder(fullQualifiedClassName).append('.');

        /*
         * constructor params become fields
         */
        params.forEach(param -> param.accept((par, arg) -> {
            String fname = getFullyQualifiedName(methodPrefix, par.lident_);
            String fdescriptor = creator.apply(par.type_, Collections.emptyList()).substring(2);
            arg.put(fname, fdescriptor);
            return arg;
        }, map));
        
        MethodBodyVisitor visitor =
            new MethodBodyVisitor(mname -> getFullyQualifiedName(methodPrefix, mname), creator);
        bodies.forEachOrdered(m -> m.accept(visitor, map));
        return map;
    }

    private static String getFullyQualifiedName(StringBuilder prefix, String name) {
        String fname = prefix.append(name).toString();
        prefix.setLength(prefix.length() - name.length());
        return fname;
    }

    private String getFullyQualifiedName(String declaration) {
        return getFullyQualifiedName(moduleNamePrefix, declaration);
    }

}
