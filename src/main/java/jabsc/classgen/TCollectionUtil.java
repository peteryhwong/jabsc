package jabsc.classgen;

import gnu.trove.impl.Constants;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

final class TCollectionUtil {
    
    static TIntList createIntList() {
        return new TIntArrayList(Constants.DEFAULT_CAPACITY, -1);
    }

    static <T> TObjectIntMap<T> createObjectIntMap() {
        return new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
    }

}
