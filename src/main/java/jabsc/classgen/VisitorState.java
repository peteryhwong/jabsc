package jabsc.classgen;

import bnfc.abs.Absyn.AnyIden;
import bnfc.abs.Absyn.AnyIdent.Visitor;
import bnfc.abs.Absyn.AnyTyIden;
import bnfc.abs.Absyn.Modul;
import bnfc.abs.Absyn.Prog;
import bnfc.abs.Absyn.QTyp;
import bnfc.abs.Absyn.QType;
import bnfc.abs.Absyn.QTypeSegmen;
import bnfc.abs.Absyn.TTyp;
import bnfc.abs.Absyn.TTypeSegmen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
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
                return p.ttype_.accept(
                    (TTyp ts, Set<String> as) -> {
                        ts.listttypesegment_.stream().forEach(
                            segt -> segt.accept((TTypeSegmen seg, Set<String> names) -> {
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
                String moduleName = p.qtype_.accept(qtypeVisitor, null);
                StringBuilder name = new StringBuilder(moduleName).append('.');
                p.listanyident_.stream().forEach(anyIdent -> {
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
                String moduleName = p.qtype_.accept(qtypeVisitor, null);
                StringBuilder name = new StringBuilder(moduleName).append('.');
                VisitorState.this.moduleInfos.get(moduleName).exports.forEach(string -> {
                    arg.add(name.append(string).toString());
                    name.setLength(name.length() - string.length());
                });
                return null;
            }

        };

    private final Map<String, String> classNames = new HashMap<>();

    private static final class ModuleInfo {
        private String name;
        private Set<String> exports;
        private Set<String> imports;
        private Map<String, String> qualifiedDeclarations;
        private Map<String, String> nameToQualifiedName;
    }

    private final Map<Modul, ModuleInfo> moduleInfos = new HashMap<>();

    private final QType.Visitor<String, Boolean> qtypeVisitor = (QTyp p, Boolean arg) -> {

        StringBuilder sb = new StringBuilder();

        p.listqtypesegment_.forEach(s -> s.accept((QTypeSegmen seg, Void v) -> {
            sb.append(seg.uident_).append('.');
            return null;
        }, null));

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
        if (info.qualifiedDeclarations.containsKey(type)) {
            return info.qualifiedDeclarations.get(type);
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

    Set<String> getModuleToExports(String moduleName) {
        return moduleInfos.get(moduleName).exports;
    }

    Set<String> getModuleToImports(String moduleName) {
        return moduleInfos.get(moduleName).imports;
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

    VisitorState buildProgramDeclarationTypes(Prog program) {

        program.listmodule_.forEach(mod -> mod.accept(
            (Modul m, Void v) -> {
                ModuleInfo info = new ModuleInfo();
                info.name = m.qtype_.accept(qtypeVisitor, Boolean.FALSE);
                info.exports =
                    m.listdecl_.stream().map(d -> StateUtil.getTopLevelDeclIdentifier(d))
                        .collect(Collectors.toSet());

                /*
                 * assume export *;
                 */
                info.qualifiedDeclarations = new HashMap<>();

                StringBuilder prefix = new StringBuilder(info.name).append('.');
                info.exports.forEach(s -> {
                    info.qualifiedDeclarations.put(s, prefix.append(s).toString());
                    prefix.setLength(prefix.length() - s.length());
                });

                moduleInfos.put(m, info);
                return null;
            }, null));

        moduleInfos.keySet().forEach(m -> {
            ModuleInfo info = moduleInfos.get(m);
            info.imports = new HashSet<>();
            m.listimport_.forEach(im -> im.accept(importVisitor, info.imports));

            info.nameToQualifiedName = new HashMap<>();
            info.imports.forEach(d -> {
                Matcher mt = StateUtil.UNQUALIFIED_CLASSNAME.matcher(d);
                mt.matches();
                info.nameToQualifiedName.put(mt.group(1), d);
            });
        });

        Set<String> interfaces =
            moduleInfos.keySet().stream().map(m -> m.listdecl_).flatMap(d -> d.stream())
                .filter(d -> StateUtil.isAbsInterfaceDecl(d))
                .map(d -> StateUtil.getTopLevelDeclIdentifier(d)).collect(Collectors.toSet());

        moduleInfos.keySet().stream().map(m -> m.listdecl_).flatMap(d -> d.stream())
            .filter(d -> StateUtil.isAbsClassDecl(d))
            .map(d -> StateUtil.getTopLevelDeclIdentifier(d)).forEach(c -> {
                if (interfaces.contains(c)) {
                    classNames.put(c, c + "Impl");
                } else {
                    classNames.put(c, c);
                }
            });

        return this;
    }
}
