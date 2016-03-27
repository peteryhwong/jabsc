package jabsc.classgen;

import gnu.trove.impl.Constants;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

final class TCollectionUtil {
    
    static final int DEFAULT_NO_ENTRY_VALUE = -1;
    
    static TIntList createIntList() {
        return new TIntArrayList(Constants.DEFAULT_CAPACITY, DEFAULT_NO_ENTRY_VALUE);
    }

    static <T> TObjectIntMap<T> createObjectIntMap() {
        return new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, DEFAULT_NO_ENTRY_VALUE);
    }

}
