package jabsc.classgen;

import bnfc.abs.Absyn.AnyIden;
import bnfc.abs.Absyn.AnyIdent.Visitor;
import bnfc.abs.Absyn.AnyTyIden;
import bnfc.abs.Absyn.Decl;
import bnfc.abs.Absyn.Modul;
import bnfc.abs.Absyn.Prog;
import bnfc.abs.Absyn.QTyp;
import bnfc.abs.Absyn.QType;
import bnfc.abs.Absyn.QTypeSegmen;
import bnfc.abs.Absyn.QTypeSegment;
import bnfc.abs.Absyn.TTyp;
import bnfc.abs.Absyn.TTypeSegmen;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

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
                VisitorState.this.imports.get(moduleName).stream().forEach(string -> {
                    arg.add(name.append(string).toString());
                    name.setLength(name.length() - string.length());
                });
                return null;
            }

        };


    private final Set<String> moduleNames = new HashSet<>();
    private final Map<AbsElementType, Set<Decl>> elements = new EnumMap<>(AbsElementType.class);
    private final Map<String, String> classNames = new HashMap<>();
    private final Set<String> packageEnumImports = new HashSet<>();
    private final Map<String, Set<String>> imports = new HashMap<>();
    private final Map<String, Set<String>> moduleToImports = new HashMap<>();

    private final QType.Visitor<String, Void> qtypeVisitor;
    private final Function<String, String> javaTypeTranslator;
    private final BiFunction<String, ElementKind, ClassWriter> classTranslator;

    private Modul currentModule = null;

    VisitorState(BiFunction<String, ElementKind, ClassWriter> classTranslator) {
        this(Function.identity(), classTranslator);
    }

    VisitorState(Function<String, String> javaTypeTranslator,
        BiFunction<String, ElementKind, ClassWriter> classTranslator) {
        this.javaTypeTranslator = javaTypeTranslator;
        this.classTranslator = classTranslator;
        this.qtypeVisitor = (QTyp p, Void arg) -> {
            StringBuilder sb = new StringBuilder();
            for (QTypeSegment seg : p.listqtypesegment_) {
                sb.append(((QTypeSegmen) seg).uident_).append('.');
            }
            return javaTypeTranslator.apply(sb.substring(0, sb.length() - 1));
        };

        EnumSet.allOf(AbsElementType.class).stream()
            .forEach(type -> this.elements.put(type, new HashSet<>()));
    }

    String processQType(QType type) {
        return type.accept(this.qtypeVisitor, null);
    }

    String getJavaType(String name) {
        return javaTypeTranslator.apply(name);
    }

    Set<String> getImports(String name) {
        return imports.get(name);
    }

    Set<String> getModuleToImports(String name) {
        return moduleToImports.get(name);
    }

    String getRefinedClassName(String name) {
        return classNames.get(name);
    }

    ClassWriter getFileWriter(String name, ElementKind kind) {
        return classTranslator.apply(name, kind);
    }

    Set<Decl> getTypes(AbsElementType type) {
        return elements.get(type);
    }

    VisitorState setCurrentModule(Modul module) {
        this.moduleNames.add(module.qtype_.accept(qtypeVisitor, null));
        this.currentModule = module;
        return this;
    }

    Modul getCurrentModule() {
        return this.currentModule;
    }

    VisitorState resetCurrentModule() {
        this.currentModule = null;
        return this;
    }

    Set<String> getPackageEnumImports() {
        return packageEnumImports;
    }

    private void processDeclaration(String name, Decl decl) {
        Set<String> values = imports.get(name);
        String declName = StateUtil.getTopLevelDeclIdentifier(decl);
        values.add(declName);

        if (StateUtil.isAbsInterfaceDecl(decl)) {
            // 1. Interfaces
            getTypes(AbsElementType.INTERFACE).add(decl);
        } else if (StateUtil.isAbsClassDecl(decl)) {
            // 2. Classes
            Predicate<Decl> pred = StateUtil.isTheSameTopLevelDeclIdentifier(declName);
            if (getTypes(AbsElementType.INTERFACE).stream().anyMatch(pred)) {
                classNames.put(declName, declName + "Impl");
            } else {
                classNames.put(declName, declName);
            }
            getTypes(AbsElementType.CLASS).add(decl);
        } else if (StateUtil.isAbsFunctionDecl(decl)) {
            // 3. Functions
            getTypes(AbsElementType.FUNCTION).add(decl);
        } else if (StateUtil.isAbsDataTypeDecl(decl)) {
            // 4. Data
            getTypes(AbsElementType.DATA).add(decl);
            packageEnumImports.add(StateUtil.getTopLevelDeclIdentifier(decl));
        } else if (StateUtil.isAbsAbstractTypeDecl(decl)) {
            // 5. Type
            getTypes(AbsElementType.TYPE).add(decl);
        }

    }

    VisitorState buildProgramDeclarationTypes(Prog program) {
        Map<Modul, String> names = new HashMap<>();

        program.listmodule_.stream().forEach(mod -> mod.accept((Modul m, Void v) -> {
            String name = m.qtype_.accept(qtypeVisitor, null);
            names.put(m, name);
            imports.put(name, new HashSet<>());
            m.listdecl_.stream().forEach(d -> processDeclaration(name, d));
            return null;
        }, null));

        program.listmodule_.stream().forEach(mod -> mod.accept((Modul m, Void v) -> {
            Set<String> importToModule = new HashSet<>();
            m.listimport_.stream().forEach(im -> im.accept(importVisitor, importToModule));
            moduleToImports.put(names.get(m), importToModule);
            return null;
        }, null));

        return this;
    }
}
