package jabsc.classgen;


import java.util.Arrays;

import gnu.trove.map.TObjectIntMap;
import javassist.bytecode.Bytecode;

final class MethodState {

    static final String THIS = "this";
    
    /**
     * Local variables
     */
    private final TObjectIntMap<String> locals = TCollectionUtil.createObjectIntMap();
    
    private final Bytecode code;
    
    /**
     * The initial index for local variables.
     */
    private int currentIndex = 0;
    
    MethodState(Bytecode code, String... param) {
        this.code = code;
        addLocalVariable(THIS);
        Arrays.stream(param).forEach(this::addLocalVariable);
    }

    int addLocalVariable(String variableName) {
        return addLocalVariable(variableName, 1);
    }

    int addLocalVariable(String variableName, int size) {
        int index = getLocalVariable(variableName);
        if (index == TCollectionUtil.DEFAULT_NO_ENTRY_VALUE) {
            locals.put(variableName, currentIndex);
            index = currentIndex;
            currentIndex += size;
            code.incMaxLocals(size);
        }
        return index;
    }

    int getLocalVariable(String variableName) {
        return locals.get(variableName);
    }

}
