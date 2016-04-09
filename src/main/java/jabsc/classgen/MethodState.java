package jabsc.classgen;


import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

import gnu.trove.map.TObjectIntMap;

final class MethodState {

    static final String THIS = "this";
    
    /**
     * Local variables
     */
    private final TObjectIntMap<String> locals = TCollectionUtil.createObjectIntMap();
    
    /**
     * Types
     */
    private final Map<String, String> types = new HashMap<>();
    
    private final IntConsumer maxLocalConsumer;
    
    private final String className;
    
    private final BootstrapMethodManager counter;
    
    /**
     * The initial index for local variables.
     */
    private int currentIndex = 0;
    
    MethodState(IntConsumer maxLocalConsumer, BootstrapMethodManager counter, 
                    String className, Map<String, String> params) {
        this.maxLocalConsumer = maxLocalConsumer;
        this.className = className;
        this.counter = counter;
        addLocalVariable(THIS, null);
        params.forEach(this::addLocalVariable);
    }
    
    String getClassName() {
        return className;
    }
    
    BootstrapMethodManager getCounter() {
        return counter;
    }

    int addLocalVariable(String variableName, String type) {
        return addLocalVariable(variableName, type, 1);
    }

    int addLocalVariable(String variableName, String type, int size) {
        int index = getLocalVariable(variableName);
        if (index == TCollectionUtil.DEFAULT_NO_ENTRY_KEY_VALUE) {
            locals.put(variableName, currentIndex);
            types.put(variableName, type);
            index = currentIndex;
            currentIndex += size;
            maxLocalConsumer.accept(size);
        }
        return index;
    }

    int getLocalVariable(String variableName) {
        return locals.get(variableName);
    }

    String getLocalVariableType(String variableName) {
        return types.get(variableName);
    }

}
