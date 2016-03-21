package jabsc.classgen;


import java.util.Arrays;

import gnu.trove.map.TObjectIntMap;

final class MethodState {

    static final String THIS = "this";
    
    /**
     * Local variables
     */
    private final TObjectIntMap<String> locals =  TCollectionUtil.createObjectIntMap();
    
    /**
     * The initial index for local variables.
     */
    private int currentIndex = 0;
    
    MethodState(String... param) {
        addLocalVariable(THIS);
        Arrays.stream(param).forEach(this::addLocalVariable);
    }

    int addLocalVariable(String variableName) {
        return addLocalVariable(variableName, 1);
    }

    int addLocalVariable(String variableName, int size) {
        int index = getLocalVariable(variableName);
        if (index == 0) {
            locals.put(variableName, currentIndex);
            index = currentIndex;
            currentIndex += size;
        }
        return index;
    }

    int getLocalVariable(String variableName) {
        return locals.get(variableName);
    }

}
