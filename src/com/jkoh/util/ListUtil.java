package com.jkoh.util;

import java.util.*;

public class ListUtil {
	private static final Random RANDOM = new Random();
	public enum ListType {
		ARRAYLIST { public <E> List<E> newList() { return new ArrayList<E>(); } },
		LINKEDLIST { public <E> List<E> newList() { return new LinkedList<E>(); } };
		public abstract <E> List<E> newList();
	}
	public static <E> int compareList(List<E> o1, List<E> o2) {
		for(int i = 0; i < (o1.size() < o2.size() ? o1.size() : o2.size()); i++) {
			int compare = compareListCompatible(o1.get(i), o2.get(i));
			if(compare != 0) return compare;
		}
		return o1.size() - o2.size();
	}
	public static <E> int compareListCompatible(E o1, E o2) {
		return o1 instanceof List ? (o2 instanceof List ? compareList((List)o1, (List)o2) : 1) :
			(o2 instanceof List ? -1 : ((Comparable)o1).compareTo(o2));
	}
	public static <E> Comparator<List<E>> listComparator() {
		return new Comparator<List<E>>() {
			public int compare(List<E> o1, List<E> o2) {
				return compareList(o1, o2);
			}
		};
	}
	public static <E> Comparator<E> listCompatibleComparator() {
		return new Comparator<E>() {
			public int compare(E o1, E o2) {
				return compareListCompatible(o1, o2);
			}
		};
	}
	
	public static <E extends Number> E sum(Collection<E> coll, Class<E> type) {
		E total = NumberUtil.cast(0, type);
		for(E value : coll) {
			total = NumberUtil.add(total, value, type);
		}
		return total;
	}
	
	public static <E> E getRandom(List<E> list) {
		if(!list.isEmpty()) {
			return list.get(RANDOM.nextInt(list.size()));
		}
		return null;
	}
	
	public static <E> E removeRandom(List<E> list) {
		if(!list.isEmpty()) {
			return list.remove(RANDOM.nextInt(list.size()));
		}
		return null;
	}
}