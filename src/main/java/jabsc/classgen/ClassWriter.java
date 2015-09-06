package jabsc.classgen;

import bnfc.abs.Absyn.Bloc;
import bnfc.abs.Absyn.MethClassBody;
import bnfc.abs.Absyn.MethSig;
import bnfc.abs.Absyn.Par;
import bnfc.abs.Absyn.Param;
import bnfc.abs.Absyn.TGen;
import bnfc.abs.Absyn.TSimple;
import bnfc.abs.Absyn.TUnderscore;
import bnfc.abs.Absyn.Type;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.ElementKind;

final class ClassWriter implements Closeable {

    private enum MethodType {
        ABSTRACT, CONCRETE, CONSTRUCTOR
    }

    private static final Pattern UNQUALIFIED_CLASSNAME = Pattern.compile("^.*\\.([^\\.]+)$");

    private final Path outputDirectory;
    private final ClassFile classFile;
    private final ConstPool constPool;

    ClassWriter(Path outputDirectory, ClassFile classFile) {
        this.outputDirectory = outputDirectory;
        this.classFile = classFile;
        this.constPool = classFile.getConstPool();
    }

    private static String getUnqualifiedName(ClassFile classFile) {
        Matcher matcher = UNQUALIFIED_CLASSNAME.matcher(classFile.getName());
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        return matcher.group(1);
    }

    @Override
    public void close() throws IOException {
        File dir = outputDirectory.toFile();
        dir.mkdirs();

        Path path = outputDirectory.resolve(getUnqualifiedName(classFile) + ".class");
        File file = path.toFile();
        file.createNewFile();
        this.classFile.write(new DataOutputStream(new FileOutputStream(file)));
    }

    private MethodInfo createMethodInfo(String name, String descriptor, MethodType methodType) {
        MethodInfo minfo = new MethodInfo(constPool, name, descriptor);
        switch (methodType) {
            case ABSTRACT:
                minfo.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.ABSTRACT);
                break;
            case CONCRETE:
                minfo.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.FINAL);
                break;
            case CONSTRUCTOR:
                minfo.setAccessFlags(AccessFlag.PUBLIC);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return minfo;
    }

    void init() {
        Bytecode code = new Bytecode(constPool);
        code.addAload(0);
        code.addInvokespecial("java/lang/Object", MethodInfo.nameInit, "()V");
        code.addReturn(null);
        code.setMaxLocals(1);

        MethodInfo minfo = createMethodInfo(MethodInfo.nameInit, "()V", MethodType.CONSTRUCTOR);
        minfo.setCodeAttribute(code.toCodeAttribute());

        try {
            classFile.addMethod(minfo);
        } catch (DuplicateMemberException e) {
            throw new IllegalStateException(e);
        }
    }

    static String translateType(String absType) {
        if (absType.equals("Unit")) {
            return "java/lang/Void";
        } else if (absType.equals("Int")) {
            return "java/lang/Integer";
        } else if (absType.equals("Bool")) {
            return "java/lang/Boolean";
        } else if (absType.equals("String")) {
            return "java/lang/String";
        } else {
            return absType;
        }
    }

    private final class TypeVisitor implements Type.Visitor<Void, StringBuilder> {

        private final VisitorState state;

        private TypeVisitor(VisitorState state) {
            this.state = state;
        }

        @Override
        public Void visit(TUnderscore p, StringBuilder arg) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Void visit(TSimple p, StringBuilder arg) {
            arg.append(translateType(state.processQType(p.qtype_).replace('.', '/')));
            return null;
        }

        @Override
        public Void visit(TGen p, StringBuilder arg) {
            throw new UnsupportedOperationException();
        }

    }

    private MethodInfo createMethodInfo(String methodName, Type returnType, List<Param> params,
        VisitorState state, MethodType methodType) {

        TypeVisitor typeVisitor = new TypeVisitor(state);
        StringBuilder descriptor = new StringBuilder();

        params.stream().forEachOrdered(
            param -> param.accept(
                (Par par, StringBuilder sb) -> par.type_.accept(typeVisitor, descriptor),
                descriptor));

        descriptor.append("()");

        returnType.accept(typeVisitor, descriptor);

        return createMethodInfo(methodName, descriptor.toString(), methodType);
    }

    void onMethod(MethSig method, VisitorState state) {
        MethodInfo minf =
            createMethodInfo(method.lident_, method.type_, method.listparam_, state,
                MethodType.ABSTRACT);
        try {
            classFile.addMethod(minf);
        } catch (DuplicateMemberException e) {
            throw new IllegalStateException(e);
        }
    }
    
    void onMethod(MethClassBody body, VisitorState state) {
        MethodInfo methodInfo =
            createMethodInfo(body.lident_, body.type_, body.listparam_, state, MethodType.CONCRETE);

        Bytecode code = new Bytecode(constPool);
        StatementVisitor statementVisitor = new StatementVisitor(state);
        
        body.block_.accept((Bloc bloc, Void v) -> {
            bloc.liststm_.stream().forEachOrdered(stm -> stm.accept(statementVisitor, code));
            return null;
        }, null);
        
        methodInfo.setCodeAttribute(code.toCodeAttribute());
        
        try {
            classFile.addMethod(methodInfo);
        } catch (DuplicateMemberException e) {
            throw new IllegalStateException(e);
        }

    }

    public static void main(String[] args) throws IOException {

        Path outputDirectory = Paths.get("/Users/pwong/projects/tmp/gen");
        ClassFileWriterSupplier supplier = new ClassFileWriterSupplier("test", outputDirectory);
        ClassWriter writer = supplier.apply("FooImpl", ElementKind.CLASS);
        writer.init();
        writer.close();

    }


}
