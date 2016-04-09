package jabsc.classgen;

import gnu.trove.impl.Constants;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

final class TCollectionUtil {
    
    @FunctionalInterface
    interface MapCreator<T, V> {
        T create(int initialCapacity, float loadFactor, V defaultKeyOrValue);
    }
    
    static final int DEFAULT_NO_ENTRY_KEY_VALUE = -1;
    
    static TIntList createIntList() {
        return new TIntArrayList(Constants.DEFAULT_CAPACITY, DEFAULT_NO_ENTRY_KEY_VALUE);
    }

    static <T> TObjectIntMap<T> createObjectIntMap() {
        return createMap(TObjectIntHashMap::new);
    }

    static <T> TIntObjectMap<T> createIntObjectMap() {
        return createMap(TIntObjectHashMap::new);
    }

    static <T> T createMap(MapCreator<T, Integer> mapCreator) {
        return mapCreator.create(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, DEFAULT_NO_ENTRY_KEY_VALUE);
    }

}
