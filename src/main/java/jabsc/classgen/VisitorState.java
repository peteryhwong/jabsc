package jabsc.classgen;

import bnfc.abs.Absyn.AnyIden;
import bnfc.abs.Absyn.AnyIdent.Visitor;
import bnfc.abs.Absyn.AnyTyIden;
import bnfc.abs.Absyn.Decl;
import bnfc.abs.Absyn.Modul;
import bnfc.abs.Absyn.Param;
import bnfc.abs.Absyn.Prog;
import bnfc.abs.Absyn.QTyp;
import bnfc.abs.Absyn.QType;
import bnfc.abs.Absyn.Type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.lang.model.element.ElementKind;

final class VisitorState {

    private final bnfc.abs.Absyn.Import.Visitor<Void, Set<String>> importVisitor =
        new bnfc.abs.Absyn.Import.Visitor<Void, Set<String>>() {

            @Override
            public Void visit(bnfc.abs.Absyn.AnyImport p, Set<String> arg) {
                /*
                 * import fully.qualified.Name1, fully.qualified.Name2, ...;
                 */
                return p.ttype_.accept((ts, as) -> {
                    ts.listttypesegment_.forEach(segt -> segt.accept((seg, names) -> {
                        names.add(seg.uident_);
                        return null;
                    }, arg));
                    return null;
                }, arg);
            }

            @Override
            public Void visit(bnfc.abs.Absyn.AnyFromImport p, Set<String> arg) {
                /*
                 * import unqualifiedName1, unqualifiedName2,.. from fully.qualified.Name;
                 */
                String moduleName = p.qtype_.accept(qtypeVisitor, Boolean.FALSE);
                StringBuilder name = new StringBuilder(moduleName).append('.');
                p.listanyident_.forEach(anyIdent -> {
                    anyIdent.accept(new Visitor<Void, Set<String>>() {

                        @Override
                        public Void visit(AnyIden p, Set<String> arg) {
                            arg.add(name.append(p.lident_).toString());
                            name.setLength(name.length() - p.lident_.length());
                            return null;
                        }

                        @Override
                        public Void visit(AnyTyIden p, Set<String> arg) {
                            arg.add(name.append(p.uident_).toString());
                            name.setLength(name.length() - p.uident_.length());
                            return null;
                        }

                    }, arg);
                });
                return null;
            }

            @Override
            public Void visit(bnfc.abs.Absyn.StarFromImport p, Set<String> arg) {
                /*
                 * import * from fully.qualified.Name;
                 */
                String moduleName = p.qtype_.accept(qtypeVisitor, Boolean.FALSE);
                StringBuilder name = new StringBuilder(moduleName).append('.');
                VisitorState.this.moduleInfos.get(modules.get(moduleName)).exports
                    .forEach(string -> {
                        arg.add(name.append(string).toString());
                        name.setLength(name.length() - string.length());
                    });
                return null;
            }

        };

    private final Map<String, String> classNames = new HashMap<>();

    static final class ModuleInfo {

        /**
         * Module name
         */
        private String name;

        /**
         * Name of the class containing function definition
         */
        private String functionClassName;

        /**
         * Name of the class containing the block
         */
        private String mainClassName;

        /**
         * Name of declarations exported by this module.
         */
        private final Set<String> exports = new HashSet<>();

        /**
         * Name of fully name imported by this module.
         */
        private final Set<String> imports = new HashSet<>();

        /**
         * Declared names to their fully qualified names
         */
        private final Map<String, String> qualifiedDeclarations = new HashMap<>();

        /**
         * Import names to their fully qualified names
         */
        private final Map<String, String> nameToQualifiedName = new HashMap<>();

        /**
         * Declared names (constructors, methods) to their signatures
         */
        private final Map<String, String> nameToSignature = new HashMap<>();
        
        public Set<String> getImports() {
            return imports;
        }
        
        public Set<String> getExports() {
            return exports;
        }
        
        public String getFunctionClassName() {
            return functionClassName;
        }
        
        public String getMainClassName() {
            return mainClassName;
        }
        
        public String getName() {
            return name;
        }
        
        public Map<String, String> getNameToQualifiedName() {
            return nameToQualifiedName;
        }
        
        public Map<String, String> getNameToSignature() {
            return nameToSignature;
        }
        
        public Map<String, String> getQualifiedDeclarations() {
            return qualifiedDeclarations;
        }

    }

    private final Map<String, Modul> modules = new HashMap<>();
    private final Map<Modul, ModuleInfo> moduleInfos = new HashMap<>();

    /**
     * {@link QType.Visitor} that takes a {@link QTyp} and returns its name in JVM
     */
    private final QType.Visitor<String, Boolean> qtypeVisitor = (p, arg) -> {

        StringBuilder sb = new StringBuilder();

        p.listqtypesegment_
            .forEach(s -> s.accept((seg, v) -> sb.append(seg.uident_).append('.'), null));

        String type = sb.substring(0, sb.length() - 1);
        if (!arg.booleanValue()) {
            return type;
        }

        if (p.listqtypesegment_.size() > 1) {
            return type;
        }

        if (StateUtil.BUILT_IN_ABS.contains(type)) {
            return type;
        }

        ModuleInfo info = VisitorState.this.currentModule;
        String fullyQualified = info.qualifiedDeclarations.get(type);
        if (fullyQualified != null) {
            return fullyQualified;
        }

        return info.nameToQualifiedName.get(type);

    };
    
    private final BiFunction<String, ElementKind, ClassWriter> classTranslator;

    private ModuleInfo currentModule = null;

    VisitorState(BiFunction<String, ElementKind, ClassWriter> classTranslator) {
        this.classTranslator = classTranslator;
    }

    String processQType(QType type) {
        return type.accept(this.qtypeVisitor, Boolean.TRUE);
    }

    String getFunctionName(Modul module) {
        return moduleInfos.get(module).functionClassName;
    }

    String getMainName(Modul module) {
        return moduleInfos.get(module).mainClassName;
    }

    Set<String> getModuleToExports(String moduleName) {
        return moduleInfos.get(modules.get(moduleName)).exports;
    }

    Set<String> getModuleToImports(String moduleName) {
        return moduleInfos.get(modules.get(moduleName)).imports;
    }

    String getRefinedClassName(String name) {
        return classNames.get(name);
    }

    ClassWriter getFileWriter(String name, ElementKind kind) {
        return classTranslator.apply(name, kind);
    }

    VisitorState setCurrentModule(Modul module) {
        this.currentModule = moduleInfos.get(module);
        return this;
    }
    
    ModuleInfo getCurrentModule() {
        return currentModule;
    }
    
    ModuleInfo getModuleInfos(String moduleName) {
        return moduleInfos.get(moduleName);
    }
    
    String getConstructorDescriptor(String fullyQualifiedClassName) {
        Matcher matcher = StateUtil.UNQUALIFIED_CLASSNAME.matcher(fullyQualifiedClassName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }

        String className = matcher.group(1);
        return getDescriptor(new StringBuilder(fullyQualifiedClassName).append('/').append(className).toString());
    }

    String getDescriptor(String fullyQualifiedName) {
        Matcher matcher = StateUtil.MODULE_NAME.matcher(fullyQualifiedName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }

        String moduleName = matcher.group(1).replace('/', '.');
        Modul mod = modules.get(moduleName);
        if (mod == null) {
            throw new IllegalArgumentException();
        }

        ModuleInfo module = moduleInfos.get(modules.get(moduleName));
        String signature = module.nameToSignature.get(fullyQualifiedName.replace('/', '.'));
        if (signature == null) {
            throw new IllegalArgumentException();
        }

        return signature;
    }

    private static void updateName(StringBuilder name, String against) {
        if (against.length() < name.length()) {
            return;
        }
        
        for (int i = 0; i < against.length(); i++) {
            if (i == name.length()) {
                name.append('1');
                break;
            }

            if (against.charAt(i) != name.charAt(i)) {
                break;
            }
        }
    }

    VisitorState buildProgramDeclarationTypes(Prog program) {

        StringBuilder function = new StringBuilder(StateUtil.FUNCTIONS_CLASS_NAME);
        StringBuilder main = new StringBuilder(StateUtil.MAIN_CLASS_NAME);

        program.listmodule_.forEach(mod -> mod.accept(
            (m, v) -> {

                ModuleInfo info = new ModuleInfo();
                moduleInfos.put(m, info);
                
                /*
                 * Module name
                 */
                info.name = m.qtype_.accept(qtypeVisitor, Boolean.FALSE);
                
                /*
                 * TODO we need to make module name to lower case as it maps to package names
                 */
                info.name = info.name.toLowerCase();
                modules.put(info.name, m);

                Consumer<String> consumeDecl = info.exports::add;
                Consumer<String> consumeDeclAndUpdate = consumeDecl
                                .andThen(s -> updateName(function, s))
                                .andThen(s -> updateName(main, s));

                Predicate<Decl> isFunction = StateUtil::isAbsFunctionDecl;
                Predicate<Decl> isNotFunction = isFunction.negate();
                
                m.listdecl_.stream().filter(isNotFunction)
                    .map(StateUtil::getTopLevelDeclIdentifier).forEach(consumeDeclAndUpdate);

                function.append('.');
                int functionLength = function.length();

                m.listdecl_.stream().filter(isFunction)
                     .map(StateUtil::getTopLevelDeclIdentifier)
                     .map(function::append)
                     .map(fun -> {
                            String qf = fun.toString();
                            fun.setLength(functionLength);
                            return qf;
                         }).forEach(consumeDecl);

                function.setLength(functionLength - 1);
                info.functionClassName = function.toString();
                function.setLength(StateUtil.FUNCTIONS_CLASS_NAME.length());

                info.mainClassName = main.toString();
                main.setLength(StateUtil.MAIN_CLASS_NAME.length());

                /*
                 * assume export *;
                 */
                StringBuilder prefix = new StringBuilder(info.name).append('.');
                info.exports.forEach(s -> {
                    info.qualifiedDeclarations.put(s, prefix.append(s).toString());
                    prefix.setLength(prefix.length() - s.length());
                });

                return null;
            }, null));

        moduleInfos.forEach((m, info) -> {
            m.listimport_.forEach(im -> im.accept(importVisitor, info.imports));
            info.imports.forEach(d -> {
                Matcher mt = StateUtil.UNQUALIFIED_CLASSNAME.matcher(d);
                mt.matches();
                info.nameToQualifiedName.put(mt.group(1), d);
            });
        });

        Set<String> interfaces =
            moduleInfos.keySet().stream().map(m -> m.listdecl_).flatMap(d -> d.stream())
                .filter(StateUtil::isAbsInterfaceDecl).map(StateUtil::getTopLevelDeclIdentifier)
                .collect(Collectors.toSet());

        moduleInfos.keySet().stream().map(m -> m.listdecl_).flatMap(d -> d.stream())
            .filter(StateUtil::isAbsClassDecl).map(StateUtil::getTopLevelDeclIdentifier)
            .forEach(c -> {
                if (interfaces.contains(c)) {
                    classNames.put(c, c + "Impl");
                } else {
                    classNames.put(c, c);
                }
            });

        BiFunction<Type, List<Param>, String> descriptorCreator =
            new DescriptorCreator().apply(this::processQType);

        moduleInfos.forEach((m, info) -> m.listdecl_.forEach(d -> d.accept(new DescriptorVisitor(
            info.name, classNames::get, descriptorCreator), info.nameToSignature)));

        return this;
    }
}
