package com.jkoh.util;

import java.util.*;

public class MapUtil {
	private static final Random RANDOM = new Random();
	public enum MapType {
		HASHMAP { public <K, V> Map<K, V> newMap() { return new HashMap<K, V>(); } },
		LINKEDHASHMAP { public <K, V> Map<K, V> newMap() { return new LinkedHashMap<K, V>(); } },
		TREEMAP { public <K, V> Map<K, V> newMap() { return new TreeMap<K, V>(); } 
			public <K, V> Map<K, V> newMap(Comparator<? super K> comparator) { return new TreeMap<K, V>(comparator); } };
		public abstract <K, V> Map<K, V> newMap();
		public <K, V> Map<K, V> newMap(Comparator<? super K> comparator) { throw new UnsupportedOperationException("Comparator not supported for " + this); }
	}
	public enum NavigableMapType {
		TREEMAP { public <K, V> NavigableMap<K, V> newNavigableMap() { return new TreeMap<K, V>(); } 
			public <K, V> NavigableMap<K, V> newNavigableMap(Comparator<? super K> comparator) { return new TreeMap<K, V>(comparator); } };
		public abstract <K, V> NavigableMap<K, V> newNavigableMap();
		public abstract <K, V> NavigableMap<K, V> newNavigableMap(Comparator<? super K> comparator);
	}
	public static <K, V> Map<K, V> createMapSingleEntry(K key, V value, MapType mapType) {
		return createMapSingleEntry(key, value, mapType, null);
	}
	public static <K, V> Map<K, V> createMapSingleEntry(K key, V value, MapType mapType, Comparator<? super K> comparator) {
		return createMap(Arrays.asList(key), Arrays.asList(value), mapType, comparator);
	}
	public static <K, V> Map<K, V> createMap(List<K> keys, List<V> values, MapType mapType) {
		return createMap(keys, values, mapType, null);
	}
	public static <K, V> Map<K, V> createMap(List<K> keys, List<V> values, MapType mapType, Comparator<? super K> comparator) {
		if(keys.size() != values.size()) throw new IllegalArgumentException("keys size(" + keys.size() + ") != values size(" + values.size() + ")");
		Map<K, V> map = comparator != null ? mapType.<K, V>newMap(comparator) : mapType.<K, V>newMap();
		for(int i = 0; i < keys.size(); i++) {
			map.put(keys.get(i), values.get(i));
		}
		return map;
	}
	public static <K, V> NavigableMap<K, V> createNavigableMap(List<K> keys, List<V> values, NavigableMapType mapType) {
		return createNavigableMap(keys, values, mapType, null);
	}
	public static <K, V> NavigableMap<K, V> createNavigableMap(List<K> keys, List<V> values, NavigableMapType mapType, Comparator<? super K> comparator) {
		if(keys.size() != values.size()) throw new IllegalArgumentException("keys size(" + keys.size() + ") != values size(" + values.size() + ")");
		NavigableMap<K, V> map = comparator != null ? mapType.<K, V>newNavigableMap(comparator) : mapType.<K, V>newNavigableMap();
		for(int i = 0; i < keys.size(); i++) {
			map.put(keys.get(i), values.get(i));
		}
		return map;
	}
	public static <K, K2, V> void initInnerMap(Map<K, Map<K2, V>> map, K key, MapType mapType) {
		initInnerMap(map, key, mapType, null);
	}
	public static <K, K2, V> void initInnerMap(Map<K, Map<K2, V>> map, K key, MapType mapType, Comparator<? super K2> comparator) {
		if(!map.containsKey(key)) {
			map.put(key, comparator != null ? mapType.<K2, V>newMap(comparator) : mapType.<K2, V>newMap());
		}
	}
	public static <K, K2, V> void initInnerNavigableMap(Map<K, NavigableMap<K2, V>> map, K key, NavigableMapType mapType) {
		initInnerNavigableMap(map, key, mapType, null);
	}
	public static <K, K2, V> void initInnerNavigableMap(Map<K, NavigableMap<K2, V>> map, K key, NavigableMapType mapType, Comparator<? super K2> comparator) {
		if(!map.containsKey(key)) {
			map.put(key, comparator != null ? mapType.<K2, V>newNavigableMap(comparator) : mapType.<K2, V>newNavigableMap());
		}
	}
	public static <K, E> void initInnerList(Map<K, List<E>> map, K key, ListUtil.ListType listType) {
		if(!map.containsKey(key)) {
			map.put(key, listType.<E>newList());
		}
	}
	public static <K, E> void initInnerSet(Map<K, Set<E>> map, K key, SetUtil.SetType setType) {
		if(!map.containsKey(key)) {
			map.put(key, setType.<E>newSet());
		}
	}
	public static <K, V> boolean chainContainsKey(Map<K, V> map, Object ... keys) {
		return chainContainsKey(map, Arrays.asList(keys));
	}
	public static <K, V> boolean chainContainsKey(Map<K, V> map, List<Object> keys) {
		if(keys.isEmpty()) return false;
		else if(!map.containsKey(keys.get(0))) return false;
		else if(keys.size() == 1) return true;
		else if(!(map.get(keys.get(0)) instanceof Map)) return false;
		else return chainContainsKey((Map)map.get(keys.get(0)), keys.subList(1, keys.size()));
	}
	public static <T> T chainGet(Map map, Object ... keys) {
		return chainGet(map, Arrays.asList(keys));
	}
	public static <T> T chainGet(Map map, List<Object> keys) {
		if(keys.isEmpty()) return null;
		else if(!map.containsKey(keys.get(0))) return null;
		else if(keys.size() == 1) return (T)map.get(keys.get(0));
		else if(!(map.get(keys.get(0)) instanceof Map)) return null;
		else return chainGet((Map)map.get(keys.get(0)), keys.subList(1, keys.size()));
	}
	public static <K> void incrementValue(Map<K, Integer> map, K key) {
		incrementValue(map, key, 1);
	}
	public static <K, V extends Number> void incrementValue(Map<K, V> map, K key, Class<V> type) {
		incrementValue(map, key, 1, type);
	}
	public static <K> void incrementValue(Map<K, Integer> map, K key, Integer increment) {
		incrementValue(map, key, increment, int.class);
	}
	public static <K> void decrementValue(Map<K, Integer> map, K key) {
		incrementValue(map, key, -1);
	}
	public static <K, V extends Number> void decrementValue(Map<K, V> map, K key, Class<V> type) {
		incrementValue(map, key, -1, type);
	}
	public static <K> void decrementValue(Map<K, Integer> map, K key, Integer increment) {
		incrementValue(map, key, -increment, int.class);
	}
	public static <K, V extends Number> void incrementValue(Map<K, V> map, K key, Number increment, Class<V> type) {
		Number currentCount = map.containsKey(key) ? map.get(key) : 0;
		map.put(key, NumberUtil.add(currentCount, increment, type));
	}
	public static <K> boolean removeZero(Map<K, ? extends Number> map, K key) {
		if(map.containsKey(key) && map.get(key).doubleValue() == 0) {
			return map.remove(key) != null;
		}
		return false;
	}
	public static <K> boolean removeNegative(Map<K, ? extends Number> map, K key) {
		if(map.containsKey(key) && map.get(key).doubleValue() < 0) {
			return map.remove(key) != null;
		}
		return false;
	}
	public static <K> boolean removeZeroOrNegative(Map<K, ? extends Number> map, K key) {
		if(map.containsKey(key) && map.get(key).doubleValue() <= 0) {
			return map.remove(key) != null;
		}
		return false;
	}
	public static <K, K2, V> boolean removeEmpty(Map<K, ? extends Map<K2, V>> map, K key) {
		if(map.containsKey(key) && map.get(key).isEmpty()) {
			return map.remove(key) != null;
		}
		return false;
	}
	public static <K, V extends Number> double calculatePercentage(Map<K, V> numerators, Map<K, V> denominators, K key) {
		double numerator = numerators.containsKey(key) ? numerators.get(key).doubleValue() : 0;
		double denominator = denominators.containsKey(key) ? denominators.get(key).doubleValue() : 0;
		return denominator > 0 ? numerator / denominator : 0;
	}
	public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> mapValueComparator() {
		return mapValueComparator(true);
	}
	public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> mapValueComparator(final boolean ascending) {
		return new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				int compare = o1.getValue().compareTo(o2.getValue());
                return ascending ? compare : -compare;
            }
        };
	}
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map){
		return sortByValue(map, true);
	}
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean ascending){
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, MapUtil.<K, V>mapValueComparator(ascending));
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)  {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
	public static <V> V rollProbabilities(NavigableMap<Integer, V> probabilities) {
		return probabilities.higherEntry(RANDOM.nextInt(probabilities.lastKey())).getValue();
	}
}