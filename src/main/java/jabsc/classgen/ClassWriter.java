package jabsc.classgen;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import bnfc.abs.Absyn.FieldAssignClassBody;
import bnfc.abs.Absyn.FieldClassBody;
import bnfc.abs.Absyn.MethClassBody;
import bnfc.abs.Absyn.MethSig;
import bnfc.abs.Absyn.Param;
import bnfc.abs.Absyn.QType;
import bnfc.abs.Absyn.Stm;
import bnfc.abs.Absyn.Type;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

final class ClassWriter implements Closeable {

    private enum MethodType {
        ABSTRACT, CONCRETE, CONSTRUCTOR, STATIC
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
        outputDirectory.toFile().mkdirs();
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
            case STATIC:
                minfo.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC | AccessFlag.FINAL);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return minfo;
    }

    void setInterfaces(List<QType> interfaces, VisitorState state) {
        String[] nameArray = new String[interfaces.size() + 1];
        if (! interfaces.isEmpty()) {
            interfaces.stream().map(state::processQType).map(s -> s.replace('.', '/'))
                    .collect(Collectors.toSet()).toArray(nameArray);
        }
        nameArray[nameArray.length - 1] = StateUtil.ACTOR;
        classFile.setInterfaces(nameArray);
    }

    /**
     * Add a field to this class.
     * 
     * @param body
     * @param state
     */
    private void addField(FieldClassBody body, TypeVisitor typeVisitor, VisitorState state) {
        StringBuilder builder = new StringBuilder();
        body.type_.accept(typeVisitor, builder);
        addField(body.lident_, builder.append(';').toString());
    }

    private FieldInfo addField(String name, String type) {
        FieldInfo info = new FieldInfo(constPool, name, type);
        info.setAccessFlags(AccessFlag.PRIVATE);
        classFile.addField2(info);
        return info;
    }
    
    private Bytecode addFields(List<FieldAssignClassBody> bodies, Bytecode code, 
                    MethodState methodState, VisitorState state) {
        if (bodies.isEmpty()) {
            return code;
        }

        PureExpVisitor visitor = new PureExpVisitor(methodState, state);
        TypeVisitor typeVisitor = new TypeVisitor(state::processQType);
        StringBuilder builder = new StringBuilder();
        bodies.forEach(b -> {
            builder.setLength(0);
            b.type_.accept(typeVisitor, builder);
            String type = builder.append(';').toString();
            addField(b.lident_, type);
            code.addAload(0);
            b.pureexp_.accept(visitor, code);
            code.addPutfield(classFile.getName(), b.lident_, type);
        });

        return code;
    }

    private Bytecode addClassParams(List<Param> params, Bytecode code, 
                    MethodState methodState, VisitorState state) {
        if (params.isEmpty()) {
            return code;
        }

        TypeVisitor typeVisitor = new TypeVisitor(state::processQType);
        StringBuilder builder = new StringBuilder();
        params.forEach(param -> {
            param.accept((par, bcode) -> {
                builder.setLength(0);
                par.type_.accept(typeVisitor, builder);
                String type = builder.append(';').toString();
                addField(par.lident_, type);
                bcode.addAload(0);
                bcode.addAload(methodState.getLocalVariable(par.lident_));
                bcode.addPutfield(classFile.getName(), par.lident_, type);
                return bcode;
            }, code);
        });

        return code;
    }

    /**
     * Creates a constructor for this class.
     * 
     * @param params
     * @param statements
     * @param fields
     * @param fieldAssigns
     * @param state
     */
    void init(List<Param> params, List<Stm> statements, List<FieldClassBody> fields,
        List<FieldAssignClassBody> fieldAssigns, VisitorState state) {

        /*
         * initialise fields
         */
        TypeVisitor typeVisitor = new TypeVisitor(state::processQType);
        fields.forEach(f -> addField(f, typeVisitor, state));

        Bytecode code = new Bytecode(constPool);
        MethodState methodState = new MethodState(code::incMaxLocals, constPool.getClassName(), getParams(typeVisitor, params));
        
        /*
         * first local variable holds the reference of this.
         */
        code.incMaxLocals(1);
        code.addAload(0);
        code.addInvokespecial(StateUtil.OBJECT, MethodInfo.nameInit, "()V");

        /*
         * Register this object as an actor
         */
        code.addAload(0);
        String desc = new StringBuilder("()L").append(StateUtil.CONTEXT).append(';').toString();
        code.addInvokevirtual(classFile.getName(), "context", desc);
        code.addAload(0);
        code.addInvokevirtual(classFile.getName(), "toString", "()Ljava/lang/String;");
        code.addAload(0);
        code.addInvokeinterface(StateUtil.CONTEXT, "newActor",
            "(Ljava/lang/String;Ljava/lang/Object;)Labs/api/Actor;", 3);
        code.addOpcode(Opcode.POP);

        /*
         * For every parameter, add and set field
         */
        addClassParams(params, code, methodState, state);

        /*
         * For every field assign, add and set field
         */
        addFields(fieldAssigns, code, methodState, state);

        if (!statements.isEmpty()) {
            statements.forEach(stmt -> stmt.accept(new StatementVisitor(methodState, state), code));
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

    private static String createDescriptor(Type returnType, List<Param> params, VisitorState state) {
        TypeVisitor typeVisitor = new TypeVisitor(state::processQType);
        StringBuilder descriptor = new StringBuilder().append('(');

        if (!params.isEmpty()) {
            params.forEach(param -> param.accept((par, sb) -> {
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

        return descriptor.toString();
    }

    private MethodInfo createMethodInfo(String methodName, Type returnType, List<Param> params,
        VisitorState state, MethodType methodType) {
        String decriptor = ClassWriter.createDescriptor(returnType, params, state);
        return createMethodInfo(methodName, decriptor, methodType);
    }

    /**
     * Adds a method signature for this interface class.
     * 
     * @param body
     * @param state
     */
    void addMethod(MethSig method, VisitorState state) {
        classFile.addMethod2(createMethodInfo(method.lident_, method.type_, method.listparam_,
            state, MethodType.ABSTRACT));
    }

    /**
     * Add a static main method to this class.
     * 
     * @param statements
     * @param state
     */
    void addMainMethod(List<Stm> statements, VisitorState state) {
        MethodInfo instanceMethod = createMethodInfo("main", "()V", MethodType.CONCRETE);
        Bytecode code = new Bytecode(constPool);

        /*
         * first variable points to this
         */
        code.incMaxLocals(1);
        MethodState methodState = new MethodState(code::incMaxLocals, constPool.getClassName(), Collections.emptyMap());
        StatementVisitor statementVisitor = new StatementVisitor(methodState, state);
        statements.forEach(stm -> stm.accept(statementVisitor, code));
        code.addReturn(null);
        instanceMethod.setCodeAttribute(code.toCodeAttribute());
        classFile.addMethod2(instanceMethod);

        MethodInfo staticMethod =
            createMethodInfo("main", "([Ljava/lang/String;)V", MethodType.STATIC);
        /*
         * The main method
         */
        Bytecode staticCode = new Bytecode(constPool);

        /*
         * create context
         */
        staticCode.addInvokestatic("abs/api/Configuration", "newConfiguration",
            "()Labs/api/ConfigurationBuilder;");
        staticCode.addInvokevirtual("abs/api/ConfigurationBuilder", "buildContext",
            "()Labs/api/Context;");
        
        /*
         * pop to local variable 1
         */
        staticCode.addAstore(1);
        staticCode.addNew(classFile.getName());

        /*
         * duplicate the reference to the new object
         */
        staticCode.addOpcode(Opcode.DUP);

        /*
         * Initialize object
         */
        staticCode.addInvokespecial(classFile.getName(), MethodInfo.nameInit, "()V");

        /*
         * pop to local variable 2
         */
        staticCode.addAstore(2);

        /*
         * push from local variable 2
         */
        staticCode.addAload(2);
        staticCode.addInvokevirtual(classFile.getName(), "main", "()V");
        
        /*
         * push from local variable 1
         */
        staticCode.addAload(1);
        
        /*
         * stop context
         */
        staticCode.addInvokeinterface("abs/api/Context", "stop", "()V", 1);
        staticCode.addReturn(null);

        /*
         * first variable points to the input string array
         */
        staticCode.setMaxLocals(3);
        staticMethod.setCodeAttribute(staticCode.toCodeAttribute());

        /*
         * main method throws Exception.
         */
        ExceptionsAttribute cattr = new ExceptionsAttribute(constPool);
        cattr.setExceptions(new String[] {"java/lang/Exception"});
        staticMethod.setExceptionsAttribute(cattr);

        classFile.addMethod2(staticMethod);
    }
    
    private Map<String, String> getParams(TypeVisitor typeVisitor, List<Param> params) {
        return params.stream().collect(Collectors.toMap(param -> param.accept((par, v) -> par.lident_, null), 
                    param -> param.accept((par, v) -> par.type_.accept(typeVisitor, new StringBuilder()).toString(), null)));            
    }
    
    /**
     * Adds a method for this class.
     * 
     * @param body
     * @param state
     */
    void addMethod(MethClassBody body, VisitorState state) {
        Bytecode code = new Bytecode(constPool);
        
        /*
         * Capture method parameters
         */
        TypeVisitor typeVisitor = new TypeVisitor(state::processQType);
        MethodState methodState = new MethodState(code::incMaxLocals, constPool.getClassName(), getParams(typeVisitor, body.listparam_));
        StatementVisitor statementVisitor = new StatementVisitor(methodState, state);

        body.block_.accept((bloc, v) -> {
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
        
        /*
         * TODO StackMapTable
         * StackMapTable.Writer writer = new StackMapTable.Writer(32);
         */
        methodInfo.setCodeAttribute(code.toCodeAttribute());
        classFile.addMethod2(methodInfo);
    }


}
