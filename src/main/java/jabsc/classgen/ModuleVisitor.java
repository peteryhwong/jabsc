package jabsc.classgen;

import bnfc.abs.AbstractVisitor;
import bnfc.abs.Absyn.Bloc;
import bnfc.abs.Absyn.ClassDecl;
import bnfc.abs.Absyn.ClassImplements;
import bnfc.abs.Absyn.ClassParamDecl;
import bnfc.abs.Absyn.ClassParamImplements;
import bnfc.abs.Absyn.FieldAssignClassBody;
import bnfc.abs.Absyn.FieldClassBody;
import bnfc.abs.Absyn.InterfDecl;
import bnfc.abs.Absyn.JustBlock;
import bnfc.abs.Absyn.ListClassBody;
import bnfc.abs.Absyn.MaybeBlock;
import bnfc.abs.Absyn.MethClassBody;
import bnfc.abs.Absyn.MethSig;
import bnfc.abs.Absyn.Modul;
import bnfc.abs.Absyn.NoBlock;
import bnfc.abs.Absyn.Param;
import bnfc.abs.Absyn.QType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ElementKind;

final class ModuleVisitor extends AbstractVisitor<Void, ClassWriter> {

    private final VisitorState state;

    ModuleVisitor(VisitorState state) {
        this.state = state;
    }

    @Override
    public Void visit(Modul m, ClassWriter writer) {
        state.setCurrentModule(m);
        m.listdecl_.stream().forEach(decl -> decl.accept(this, writer));
        // visitFunctions(m, writer);
        // visitMain(m, writer);
        return null;
    }

    @Override
    public Void visit(InterfDecl inf, ClassWriter writer) {
        String name = inf.uident_;
        try (ClassWriter declWriter = state.getFileWriter(name, ElementKind.INTERFACE)) {
            declWriter.setInterfaces(Collections.emptyList(), state);
            inf.listmethsignat_.stream().forEachOrdered(method -> method.accept(this, declWriter));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }

    private void visit(String name, List<Param> params, ListClassBody body1, MaybeBlock block,
        ListClassBody body2, List<QType> interfaces) {
        String refinedClassName = state.getRefinedClassName(name);
        try (ClassWriter declWriter = state.getFileWriter(refinedClassName, ElementKind.CLASS)) {
            declWriter.setInterfaces(interfaces, state);
            body1.stream().forEachOrdered(cb -> cb.accept(this, declWriter));
            block.accept(new MaybeBlock.Visitor<Void, ClassWriter>() {

                @Override
                public Void visit(JustBlock p, ClassWriter arg) {
                    p.block_.accept((Bloc b, ClassWriter w) -> {
                        w.init(params, b.liststm_, state);
                        return null;
                    }, arg);
                    return null;
                }

                @Override
                public Void visit(NoBlock p, ClassWriter arg) {
                    declWriter.init(params, Collections.emptyList(), state);
                    return null;
                }

            }, declWriter);
            body2.stream().forEachOrdered(cb -> cb.accept(this, declWriter));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Void visit(ClassDecl klass, ClassWriter writer) {
        visit(klass.uident_, Collections.emptyList(), klass.listclassbody_1, klass.maybeblock_,
            klass.listclassbody_2, Collections.emptyList());
        return null;
    }

    public Void visit(MethSig sig, ClassWriter writer) {
        writer.addMethod(sig, state);
        return null;
    }

    @Override
    public Void visit(MethClassBody body, ClassWriter writer) {
        writer.addMethod(body, state);
        return null;
    }

    @Override
    public Void visit(ClassParamDecl klass, ClassWriter writer) {
        visit(klass.uident_, klass.listparam_, klass.listclassbody_1, klass.maybeblock_,
            klass.listclassbody_2, Collections.emptyList());
        return null;
    }

    @Override
    public Void visit(ClassImplements klass, ClassWriter writer) {
        visit(klass.uident_, Collections.emptyList(), klass.listclassbody_1, klass.maybeblock_,
            klass.listclassbody_2, klass.listqtype_);
        return null;
    }

    @Override
    public Void visit(ClassParamImplements klass, ClassWriter writer) {
        visit(klass.uident_, klass.listparam_, klass.listclassbody_1, klass.maybeblock_,
            klass.listclassbody_2, klass.listqtype_);
        return null;
    }

    @Override
    public Void visit(FieldAssignClassBody body, ClassWriter writer) {
        writer.addField(body, state);
        return null;
    }

    @Override
    public Void visit(FieldClassBody body, ClassWriter writer) {
        writer.addField(body, state);
        return null;
    }

}
