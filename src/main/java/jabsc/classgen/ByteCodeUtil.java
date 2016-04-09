package jabsc.classgen;

import gnu.trove.TIntCollection;
import gnu.trove.set.hash.TIntHashSet;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

final class ByteCodeUtil {

    static final TIntCollection BRANCHING_INSTRUCTIONS = new TIntHashSet(new int[] {Opcode.IFEQ,
            Opcode.IFGE, Opcode.IFGT, Opcode.IFLE, Opcode.IFLT, Opcode.IFNE, Opcode.IFNONNULL,
            Opcode.IFNULL, Opcode.IF_ACMPEQ, Opcode.IF_ACMPNE, Opcode.IF_ICMPEQ, Opcode.IF_ICMPGE,
            Opcode.IF_ICMPGT, Opcode.IF_ICMPLE, Opcode.IF_ICMPLT, Opcode.IF_ICMPNE, Opcode.GOTO});

    static class TwoBytes {

        private final int firstByte;
        private final int secondByte;

        private TwoBytes(int firstByte, int secondByte) {
            this.firstByte = firstByte;
            this.secondByte = secondByte;
        }

        static TwoBytes fromOffet(int offset) {
            int firstByte = (offset >> 8) & 0xff;
            int secondByte = offset & 0xff;
            return new TwoBytes(firstByte, secondByte);
        }

        /**
         * Joins two branch bytes as the offset.
         * @return
         */
        int toOffset() {
            return ByteCodeUtil.offset(firstByte, secondByte);
        }

        int getFirstByte() {
            return firstByte;
        }

        int getSecondByte() {
            return secondByte;
        }
    }

    /**
     * Joins two branch bytes as the offset.
     * 
     * @param firstByte
     * @param secondByte
     * @return
     */
    static int offset(int firstByte, int secondByte) {
        return ((firstByte << 8) & 0xff) | (secondByte & 0xff);
    }

    static Bytecode addGoto(Bytecode bytecode, int offset) {
        return addBranch(bytecode, Opcode.GOTO, offset);
    }

    /**
     * Adds a branching instruction with offset.
     * 
     * @param bytecode
     * @param branch
     * @param offset
     * @return
     */
    static Bytecode addBranch(Bytecode bytecode, int branch, int offset) {
        if (!BRANCHING_INSTRUCTIONS.contains(branch)) {
            throw new IllegalArgumentException();
        }
        bytecode.addOpcode(branch);
        int firstByte = (offset >> 8) & 0xff;
        int secondByte = offset & 0xff;
        bytecode.add(firstByte, secondByte);
        return bytecode;
    }

    /**
     * Add {@link Bytecode} in {@code from} to {@link Bytecode} {@code to}.
     * 
     * @param from
     * @param to the {@link Bytecode} to be updated
     * @return the updated {@link Bytecode}
     */
    static Bytecode add(Bytecode from, Bytecode to) {
        int size = from.length();
        int existingSize = to.length();
        for (int index = 0; index < size; index++) {
            int byteCode = from.read(index);
            if (BRANCHING_INSTRUCTIONS.contains(byteCode)) {
                int offset = offset(from.read(index++), from.read(index++)) + existingSize;
                addBranch(to, byteCode, offset);
            } else {
                to.add(byteCode);
            }
        }

        /*
         * increase operand stack size
         */
        to.setStackDepth(to.getMaxStack() + from.getMaxStack());
        return to;
    }
    
    /**
     * Add an invokestatic instruction to convert int to {@link Integer}.
     * 
     * @param arg {@link Bytecode}
     * @return the updated {@link Bytecode}
     */
    static Bytecode toInteger(Bytecode arg) {
        arg.addInvokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        return arg;
    }

    /**
     * Add an invokevirtual instruction to convert {@link Integer} to int.
     * 
     * @param arg
     * @return
     */
    static Bytecode toIntegerValue(Bytecode arg) {
        arg.addInvokevirtual("java/lang/Integer", "intValue", "()I");
        return arg;
    }

    /**
     * Add an invokestatic instruction to convert long to {@link Long}.
     * 
     * @param arg {@link Bytecode}
     * @return the updated {@link Bytecode}
     */
    static Bytecode toLong(Bytecode arg) {
        arg.addInvokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        return arg;
    }

    /**
     * Add an invokevirtual instruction to convert {@link Long} to long.
     * 
     * @param arg
     * @return
     */
    static Bytecode toLongValue(Bytecode arg) {
        arg.addInvokevirtual("java/lang/Long", "longValue", "()J");
        return arg;
    }

    /**
     * Add an invokestatic instruction to convert boolean to {@link Boolean}.
     * 
     * @param arg {@link Bytecode}
     * @return the updated {@link Bytecode}
     */
    static Bytecode toBoolean(Bytecode arg) {
        arg.addInvokestatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
        return arg;
    }

    /**
     * Add an invokevirtual instruction to convert {@link Boolean} to boolean.
     * 
     * @param arg
     * @return
     */
    static Bytecode toBooleanValue(Bytecode arg) {
        arg.addInvokevirtual("java/lang/Boolean", "booleanValue", "()Z");
        return arg;
    }
    
    static MethodInfo buildLambdaMethod(ConstPool cp, String className, String desc, Bytecode bytecode) {
        MethodInfo info = new MethodInfo(cp, className, desc);
        info.setAccessFlags(AccessFlag.SYNTHETIC | AccessFlag.STATIC | AccessFlag.PRIVATE);
        info.setCodeAttribute(bytecode.toCodeAttribute());
        ExceptionsAttribute exceptionsAttribute = new ExceptionsAttribute(cp);
        exceptionsAttribute.setExceptions(new String[] {"java/lang/Exception"});
        info.setExceptionsAttribute(exceptionsAttribute);
        return info;
    }
    
    static String getLambdaClassName(int bootstrap) {
        return new StringBuilder("lambda$main$").append(bootstrap).toString();
    }

    static Bytecode toSupplier(Bytecode arg, int bootstrap) {
        arg.addInvokedynamic(bootstrap, "get", "(Labs/api/Response;)Ljava/util/function/Supplier;");
        return arg;
    }
    
    static String getCallableDescriptor(String classType) {
        return new StringBuilder("(").append(classType).append(";)Ljava/util/concurrent/Callable;").toString();
    }

    static Bytecode toCallable(Bytecode arg, int bootstrap, String callableDescriptor) {
        arg.addInvokedynamic(bootstrap, "call", callableDescriptor);
        return arg;
    }

}
