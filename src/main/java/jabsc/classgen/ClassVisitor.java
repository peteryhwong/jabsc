package jabsc.classgen;

import bnfc.abs.AbstractVisitor;
import bnfc.abs.Absyn.ClassDecl;
import bnfc.abs.Absyn.ClassImplements;
import bnfc.abs.Absyn.ClassParamDecl;
import bnfc.abs.Absyn.ClassParamImplements;
import bnfc.abs.Absyn.Decl;
import bnfc.abs.Absyn.FieldAssignClassBody;
import bnfc.abs.Absyn.FieldClassBody;
import bnfc.abs.Absyn.InterfDecl;
import bnfc.abs.Absyn.MethClassBody;
import bnfc.abs.Absyn.MethSig;
import bnfc.abs.Absyn.Modul;
import bnfc.abs.Absyn.Module;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.lang.model.element.ElementKind;

final class ClassVisitor extends AbstractVisitor<Void, ClassWriter> {

    private final String packageName;
    private final VisitorState state;

    ClassVisitor(Function<String, String> javaTypeTranslator,
        BiFunction<String, ElementKind, ClassWriter> classTranslator, String packageName) {
        this.packageName = packageName;
        this.state = new VisitorState(javaTypeTranslator, classTranslator);
    }

    @Override
    public Void visit(bnfc.abs.Absyn.Prog prog, ClassWriter arg) {
        state.buildProgramDeclarationTypes(prog);
        for (Module module : prog.listmodule_) {
            state.setCurrentModule((Modul) module);
            module.accept(this, arg);
        }
        return null;
    }

    @Override
    public Void visit(Modul m, ClassWriter writer) {
        // try {

        // Types
        // Should be first to ensure correct type translation.
        state.getTypes(AbsElementType.TYPE).stream()
            .forEach((Decl decl) -> decl.accept(this, writer));

        // Interfaces
        state.getTypes(AbsElementType.INTERFACE).stream()
            .forEach((Decl decl) -> decl.accept(this, writer));

        // Classes
        state.getTypes(AbsElementType.CLASS).stream()
            .forEach((Decl decl) -> decl.accept(this, writer));

        // for (Decl decl : state.getTypes(AbsElementType.CLASS)) {
        // String name = StateUtil.getTopLevelDeclIdentifier(decl);
        // String refinedClassName = state.getRefinedClassName(name);
        // ClassWriter declWriter = state.getFileWriter(refinedClassName);
        // declWriter.emitPackage(packageName);
        // visitImports(m.listimport_, declWriter);
        // decl.accept(this, declWriter);
        // close(declWriter, writer);
        // }

        // Data
        // for (Decl decl : state.getTypes(AbsElementType.DATA)) {
        // String name = StateUtil.getTopLevelDeclIdentifier(decl);
        // ClassWriter declWriter = state.getFileWriter(name);
        // declWriter.emitPackage(packageName);
        // visitImports(m.listimport_, declWriter);
        // decl.accept(this, declWriter);
        // close(declWriter, writer);
        // }

        // visitFunctions(m, writer);
        // visitMain(m, writer);

        return null;
        // } catch (IOException e) {
        // throw new RuntimeException(e);
        // }
    }

    // private void close(ClassWriter childWriter, ClassWriter parentWriter) throws IOException {
    // if (childWriter != parentWriter) {
    // childWriter.close();
    // }
    // }
    //
    // private void visitImports(final ListImport imports, ClassWriter writer) throws IOException {
    // writer.onImports(this.state, this, imports);
    // }

    @Override
    public Void visit(InterfDecl inf, ClassWriter writer) {
        String name = inf.uident_;
        try (ClassWriter declWriter = state.getFileWriter(name, ElementKind.INTERFACE)) {
            inf.listmethsignat_.stream().forEachOrdered(method -> method.accept(this, declWriter));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }

    @Override
    public Void visit(ClassDecl klass, ClassWriter writer) {
        String name = klass.uident_;
        String refinedClassName = state.getRefinedClassName(name);

        try (ClassWriter declWriter = state.getFileWriter(refinedClassName, ElementKind.CLASS)) {
            declWriter.init();
            klass.listclassbody_1.stream().forEachOrdered(cb -> cb.accept(this, declWriter));
            klass.maybeblock_.accept(this, declWriter);
            klass.listclassbody_2.stream().forEachOrdered(cb -> cb.accept(this, declWriter));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return null;
    }

    public Void visit(MethSig sig, ClassWriter writer) {
        writer.onMethod(sig, state);
        return null;
    }

    @Override
    public Void visit(MethClassBody body, ClassWriter writer) {
        writer.onMethod(body, state);
        return null;
    }


    @Override
    public Void visit(ClassParamDecl klass, ClassWriter writer) {

        return null;
    }

    @Override
    public Void visit(ClassImplements klass, ClassWriter writer) {

        return null;
    }

    @Override
    public Void visit(ClassParamImplements klass, ClassWriter writer) {

        return null;
    }

    @Override
    public Void visit(FieldAssignClassBody body, ClassWriter writer) {

        return null;
    }

    @Override
    public Void visit(FieldClassBody body, ClassWriter writer) {

        return null;
    }

}
