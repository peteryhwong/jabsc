package jabsc.classgen;

import bnfc.abs.Absyn.Bloc;
import bnfc.abs.Absyn.FieldClassBody;
import bnfc.abs.Absyn.MethClassBody;
import bnfc.abs.Absyn.MethSig;
import bnfc.abs.Absyn.Par;
import bnfc.abs.Absyn.Param;
import bnfc.abs.Absyn.QType;
import bnfc.abs.Absyn.Stm;
import bnfc.abs.Absyn.TGen;
import bnfc.abs.Absyn.TSimple;
import bnfc.abs.Absyn.TUnderscore;
import bnfc.abs.Absyn.Type;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

final class ClassWriter implements Closeable {

    private static final String OBJECT = "java/lang/Object";
    private static final String ACTOR = "abs/api/Actor";

    private enum MethodType {
        ABSTRACT, CONCRETE, CONSTRUCTOR
    }

    private static final class TypeVisitor implements Type.Visitor<Void, StringBuilder> {

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
            arg.append(StateUtil.ABS_TO_JDK.apply(state.processQType(p.qtype_).replace('.', '/')));
            return null;
        }

        @Override
        public Void visit(TGen p, StringBuilder arg) {
            throw new UnsupportedOperationException();
        }

    }

    private final Path outputDirectory;
    private final ClassFile classFile;
    private final ConstPool constPool;

    ClassWriter(Path outputDirectory, ClassFile classFile) {
        this.outputDirectory = outputDirectory;
        this.classFile = classFile;
        this.constPool = classFile.getConstPool();
    }

    private static String getUnqualifiedName(ClassFile classFile) {
        Matcher matcher = StateUtil.UNQUALIFIED_CLASSNAME.matcher(classFile.getName());
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

    void setInterfaces(List<QType> interfaces, VisitorState state) {
        String[] nameArray;
        if (interfaces.isEmpty()) {
            nameArray = new String[] {ACTOR};
        } else {
            nameArray =
                interfaces.stream().map(state::processQType).map(s -> s.replace('.', '/'))
                    .collect(Collectors.toSet()).toArray(new String[interfaces.size() + 1]);
            nameArray[nameArray.length - 1] = ACTOR;
        }
        classFile.setInterfaces(nameArray);
    }

    /**
     * Add a field to this class.
     * 
     * @param body
     * @param state
     */
    void addField(FieldClassBody body, VisitorState state) {
        StringBuilder builder = new StringBuilder('L');
        body.type_.accept(new TypeVisitor(state), builder);
        addField(body.lident_, builder.toString());
    }

    private void addField(String name, String type) {
        FieldInfo info = new FieldInfo(constPool, name, type);
        info.setAccessFlags(AccessFlag.PRIVATE);
        classFile.addField2(info);
    }

    private Bytecode setClassParam(List<Param> params, Bytecode code, VisitorState state) {
        if (params.isEmpty()) {
            return code;
        }

        TypeVisitor typeVisitor = new TypeVisitor(state);
        StringBuilder builder = new StringBuilder('L');
        params.stream().forEachOrdered(param -> {
            param.accept((Par par, Void v) -> {
                builder.setLength(1);
                par.type_.accept(typeVisitor, builder);
                String type = builder.toString();
                addField(par.lident_, type);
                code.addAload(0);
                code.addAload(constPool.getSize());
                code.addPutfield(classFile.getName(), par.lident_, type);
                return null;
            }, (Void) null);
        });
        return code;
    }

    /**
     * Creates a constructor for this class.
     * 
     * @param body
     * @param state
     */
    void init(List<Param> params, List<Stm> statements, VisitorState state) {
        final Bytecode code = new Bytecode(constPool);
        code.addAload(0);
        code.addInvokespecial(OBJECT, MethodInfo.nameInit, "()V");

        if (!params.isEmpty() || !statements.isEmpty()) {
            /*
             * For every parameter, add and set field
             */
            setClassParam(params, code, state);
            if (!statements.isEmpty()) {
                StatementVisitor statementVisitor = new StatementVisitor(state);
                statements.stream().forEachOrdered(stmt -> stmt.accept(statementVisitor, code));
            }
        }

        /*
         * Constructor does not return any value
         */
        code.addReturn(null);

        MethodInfo minfo =
            createMethodInfo(MethodInfo.nameInit, null, params, state, MethodType.CONSTRUCTOR);
        minfo.setCodeAttribute(code.toCodeAttribute());
        classFile.addMethod2(minfo);
    }

    private MethodInfo createMethodInfo(String methodName, Type returnType, List<Param> params,
        VisitorState state, MethodType methodType) {

        TypeVisitor typeVisitor = new TypeVisitor(state);
        StringBuilder descriptor = new StringBuilder().append('(');

        if (!params.isEmpty()) {
            params.forEach(param -> param.accept((Par par, StringBuilder sb) -> {
                par.type_.accept(typeVisitor, sb);
                sb.append(';');
                return null;
            }, descriptor));
        }

        descriptor.append(')');

        /*
         * null is the same as void
         */
        if (returnType == null) {
            descriptor.append('V');
        } else {
            StringBuilder retype = new StringBuilder();
            returnType.accept(typeVisitor, retype);
            if (retype.charAt(0) != 'V') {
                retype.append(';');
            }
            descriptor.append(retype);
        }

        return createMethodInfo(methodName, descriptor.toString(), methodType);
    }

    /**
     * Adds a method signature for this interface class.
     * 
     * @param body
     * @param state
     */
    void onMethod(MethSig method, VisitorState state) {
        classFile.addMethod2(createMethodInfo(method.lident_, method.type_, method.listparam_,
            state, MethodType.ABSTRACT));
    }

    /**
     * Adds a method for this class.
     * 
     * @param body
     * @param state
     */
    void onMethod(MethClassBody body, VisitorState state) {
        Bytecode code = new Bytecode(constPool);
        StatementVisitor statementVisitor = new StatementVisitor(state);

        body.block_.accept((Bloc bloc, Void v) -> {
            bloc.liststm_.forEach(stm -> stm.accept(statementVisitor, code));
            return null;
        }, null);

        MethodInfo methodInfo =
            createMethodInfo(body.lident_, body.type_, body.listparam_, state, MethodType.CONCRETE);

        /*
         * Returns void.
         */
        if (methodInfo.getDescriptor().endsWith("V")) {
            code.addReturn(null);
        }

        methodInfo.setCodeAttribute(code.toCodeAttribute());
        classFile.addMethod2(methodInfo);
    }

}
