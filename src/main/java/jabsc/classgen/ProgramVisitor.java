package jabsc.classgen;

import bnfc.abs.Absyn.Prog;
import bnfc.abs.Absyn.Program;

import java.nio.file.Path;
import java.util.function.Function;

public final class ProgramVisitor implements Program.Visitor<Void, Void> {

    private final VisitorState state;
    private final ModuleVisitor visitor;

    public ProgramVisitor(Function<String, String> javaTypeTranslator, String packageName,
        Path outputDirectory) {
        ClassFileWriterSupplier supplier =
            new ClassFileWriterSupplier(packageName, outputDirectory);
        this.state = new VisitorState(javaTypeTranslator, supplier);
        this.visitor = new ModuleVisitor(state);
    }

    @Override
    public Void visit(Prog p, Void arg) {
        state.buildProgramDeclarationTypes(p);
        p.listmodule_.stream().forEach(mod -> mod.accept(visitor, null));
        return arg;
    }

}
