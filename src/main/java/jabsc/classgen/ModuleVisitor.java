package jabsc.classgen;

import bnfc.abs.AbstractVisitor;
import bnfc.abs.Absyn.ClassBody;
import bnfc.abs.Absyn.ClassDecl;
import bnfc.abs.Absyn.ClassImplements;
import bnfc.abs.Absyn.ClassParamDecl;
import bnfc.abs.Absyn.ClassParamImplements;
import bnfc.abs.Absyn.ExtendsDecl;
import bnfc.abs.Absyn.FieldAssignClassBody;
import bnfc.abs.Absyn.FieldClassBody;
import bnfc.abs.Absyn.InterfDecl;
import bnfc.abs.Absyn.JustBlock;
import bnfc.abs.Absyn.MaybeBlock;
import bnfc.abs.Absyn.MethClassBody;
import bnfc.abs.Absyn.MethSig;
import bnfc.abs.Absyn.MethSignat;
import bnfc.abs.Absyn.Modul;
import bnfc.abs.Absyn.NoBlock;
import bnfc.abs.Absyn.Param;
import bnfc.abs.Absyn.QType;
import bnfc.abs.Absyn.Stm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
        m.maybeblock_.accept(new MaybeBlock.Visitor<Void, ClassWriter>() {

            @Override
            public Void visit(JustBlock p, ClassWriter arg) {
                p.block_.accept((b, v) -> createMain(m, b.liststm_), null);
                return null;
            }

            @Override
            public Void visit(NoBlock p, ClassWriter arg) {
                return null;
            }

        }, writer);
        return null;
    }

    @Override
    public Void visit(InterfDecl inf, ClassWriter writer) {
        createInterface(inf.uident_, inf.listmethsignat_, Collections.emptyList());
        return null;
    }

    @Override
    public Void visit(ExtendsDecl inf, ClassWriter writer) {
        createInterface(inf.uident_, inf.listmethsignat_, inf.listqtype_);
        return null;
    }

    private void createInterface(String name, List<MethSignat> methods, List<QType> supertypes) {
        try (ClassWriter declWriter = state.getFileWriter(name, ElementKind.INTERFACE)) {
            declWriter.setInterfaces(supertypes, state);
            methods.forEach(method -> method.accept(this, declWriter));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Void createMain(Modul module, List<Stm> liststm) {
        try (ClassWriter declWriter =
            state.getFileWriter(state.getMainName(module), ElementKind.CLASS)) {
            declWriter.init(Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), state);
            declWriter.addMainMethod(liststm, state);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }

    private void createClass(String name, List<Param> params, List<ClassBody> body1,
        MaybeBlock block, List<ClassBody> body2, List<QType> interfaces) {
        String refinedClassName = state.getRefinedClassName(name);
        try (ClassWriter declWriter = state.getFileWriter(refinedClassName, ElementKind.CLASS)) {
            declWriter.setInterfaces(interfaces, state);

            List<FieldAssignClassBody> fieldAssigns = new ArrayList<>();
            List<FieldClassBody> fields = new ArrayList<>();
            Stream.concat(body1.stream(), body2.stream()).forEachOrdered(
                c -> c.accept(new ClassBody.Visitor<Void, Void>() {

                    @Override
                    public Void visit(FieldClassBody p, Void arg) {
                        fields.add(p);
                        return null;
                    }

                    @Override
                    public Void visit(FieldAssignClassBody p, Void arg) {
                        fieldAssigns.add(p);
                        return null;
                    }

                    @Override
                    public Void visit(MethClassBody p, Void arg) {
                        return null;
                    }

                }, null));

            body1.forEach(cb -> cb.accept(this, declWriter));
            block.accept(new MaybeBlock.Visitor<Void, ClassWriter>() {

                @Override
                public Void visit(JustBlock p, ClassWriter arg) {
                    p.block_.accept((b, w) -> {
                        w.init(params, b.liststm_, fields, fieldAssigns, state);
                        return null;
                    }, arg);
                    return null;
                }

                @Override
                public Void visit(NoBlock p, ClassWriter arg) {
                    arg.init(params, Collections.emptyList(), fields, fieldAssigns, state);
                    return null;
                }

            }, declWriter);
            body2.forEach(cb -> cb.accept(this, declWriter));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Void visit(ClassDecl klass, ClassWriter writer) {
        createClass(klass.uident_, Collections.emptyList(), klass.listclassbody_1,
            klass.maybeblock_, klass.listclassbody_2, Collections.emptyList());
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
        createClass(klass.uident_, klass.listparam_, klass.listclassbody_1, klass.maybeblock_,
            klass.listclassbody_2, Collections.emptyList());
        return null;
    }

    @Override
    public Void visit(ClassImplements klass, ClassWriter writer) {
        createClass(klass.uident_, Collections.emptyList(), klass.listclassbody_1,
            klass.maybeblock_, klass.listclassbody_2, klass.listqtype_);
        return null;
    }

    @Override
    public Void visit(ClassParamImplements klass, ClassWriter writer) {
        createClass(klass.uident_, klass.listparam_, klass.listclassbody_1, klass.maybeblock_,
            klass.listclassbody_2, klass.listqtype_);
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
