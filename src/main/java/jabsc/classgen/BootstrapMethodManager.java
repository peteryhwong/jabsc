package jabsc.classgen;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import javassist.bytecode.BootstrapMethodsAttribute.BootstrapMethod;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

final class BootstrapMethodManager {
    
    static final class InterfaceCall {

        private final String className;
        private final String methodName;
        private final String descriptor;
        private final int argSize;

        InterfaceCall(String className, String methodName, String descriptor, int argSize) {
            this.className = className;
            this.methodName = methodName;
            this.descriptor = descriptor;
            this.argSize = argSize;                            
        }
    }
    
    static final BiFunction<ConstPool, InterfaceCall, Bytecode> CALLABLE = (constPool, interfaceCall) -> {
        /*
         * lambdaMethod
         */
        Bytecode code = new Bytecode(constPool);
        
        /*
         * load all values from method params
         */
        for (int i = 0; i < interfaceCall.argSize; i++) {
            code.addAload(i);
        }
        
        /*
         * Invoke the actual method call
         */
        code.addInvokeinterface(interfaceCall.className, interfaceCall.methodName, 
                        interfaceCall.descriptor, interfaceCall.argSize);
        
        /*
         * Return the result
         */
        code.addOpcode(Opcode.ARETURN);
        return code;
    };
    
    /*
     * Checks if the call returns null
     */
    static final BiFunction<ConstPool, InterfaceCall, Bytecode> SUPPLIER = (constPool, interfaceCall) -> {
        /*
         * lambdaMethod
         */
        Bytecode code = new Bytecode(constPool);
        
        /*
         * Invoke the actual method call
         */
        code.addInvokeinterface("abs/api/Response", "getValue", "()Ljava/lang/Object;", 1);
        
        code.addOpcode(Opcode.IFNULL);
        // 2 byte place holders for offset
        int ifNullIndex = code.currentPc();
        code.add(-1, -1);
        // 1 to represent true and 0 to represent false
        code.addIconst(1);
        
        code.addOpcode(Opcode.GOTO);
        // 2 byte place holders for offset
        int elseIndex = code.currentPc();
        code.add(-1, -1);
        // 1 to represent true and 0 to represent false
        int falseOffset = code.currentPc();
        code.addIconst(0);
        
        code.write16bit(ifNullIndex, falseOffset);
        code.write16bit(elseIndex, code.currentPc());
        ByteCodeUtil.toBoolean(code);
        
        /*
         * Return the result
         */
        code.addOpcode(Opcode.ARETURN);
        return code;
    };
                    
    
    private static final String FACTORY_CLASS = "java/lang/invoke/LambdaMetafactory";
    private static final String FACTORY_METHOD = "metafactory";
    private static final String FACTORY_METHOD_DESCRIPTOR = "(Ljava/lang/invoke/MethodHandles$Lookup;"
                    + "Ljava/lang/String;Ljava/lang/invoke/MethodType;"
                    + "Ljava/lang/invoke/MethodType;"
                    + "Ljava/lang/invoke/MethodHandle;"
                    + "Ljava/lang/invoke/MethodType;)"
                    + "Ljava/lang/invoke/CallSite;";
    
    private final Set<BootstrapMethod> bootstrapMethods = new HashSet<>();
    private final Set<MethodInfo> lambdaMethods = new HashSet<>();
    
    private int currentIndex = 0;
    
    
    Set<BootstrapMethod> getBootstrapMethods() {
        return bootstrapMethods;
    }
    
    Set<MethodInfo> getLambdaMethods() {
        return lambdaMethods;
    }
    
    private int addMethodHandleInfo(ConstPool constPool, int kind, String className, String methodName, String methodDescriptor) {
        int classNameIndex = constPool.addClassInfo(className);
        int reference_index = constPool.addMethodrefInfo(classNameIndex, methodName, methodDescriptor);
        return constPool.addMethodHandleInfo(ConstPool.REF_invokeStatic, reference_index);
    }

    int addBootstrapMethod(ConstPool constPool, InterfaceCall interfaceCall, 
                    BiFunction<ConstPool, InterfaceCall, Bytecode> lambdaProducer) {
        
        int bootstrap = currentIndex++;
        
        /*
         * lambda$main$n where n is the bootstrap index
         */
        String lambdaName = ByteCodeUtil.getLambdaClassName(bootstrap);
        
        /*
         * The signature and return type that should be enforced dynamically at invocation time.
         * This is a specialization of ()Ljava/lang/Object;.
         */
        StringBuilder stringBuilder = new StringBuilder();
        String returnType = stringBuilder.append("()")
                        .append(interfaceCall.descriptor.substring(interfaceCall.descriptor.indexOf(')') + 1))
                        .toString();
        
        stringBuilder.setLength(0);
        String lambdaDecriptor = stringBuilder
                        .append("(L")
                        .append(interfaceCall.className).append(';')
                        .append(interfaceCall.descriptor.substring(1, interfaceCall.descriptor.indexOf(')')))
                        .append(')')
                        .append(returnType.substring(2)).toString();
        
        int bootstrap_method_ref = addMethodHandleInfo(constPool, ConstPool.REF_invokeStatic, 
                        FACTORY_CLASS, FACTORY_METHOD, FACTORY_METHOD_DESCRIPTOR);
        
        int[] bootstrap_arguments = {
                /*
                 * Signature and return type of method to be implemented by the function object.
                 */
                constPool.addMethodTypeInfo(constPool.addUtf8Info("()Ljava/lang/Object;")),
                /*
                 * A direct method handle describing the implementation method which should be
                 * called (with suitable adaptation of argument types, return types, and with
                 * captured arguments prepended to the invocation arguments) at invocation time.
                 */
                addMethodHandleInfo(constPool, ConstPool.REF_invokeStatic, 
                                constPool.getClassName(), lambdaName, lambdaDecriptor),
                /*
                 * The signature and return type that should be enforced dynamically at invocation
                 * time.
                 */
                constPool.addMethodTypeInfo(constPool.addUtf8Info(returnType)),
        };
        BootstrapMethod method = new BootstrapMethod(bootstrap_method_ref, bootstrap_arguments);
        bootstrapMethods.add(method);
        
        /*
         * lambdaMethod
         */
        Bytecode code = lambdaProducer.apply(constPool, interfaceCall);
        lambdaMethods.add(ByteCodeUtil.buildLambdaMethod(constPool, lambdaName, lambdaDecriptor, code));   
        
        return bootstrap;
    }
    
}
