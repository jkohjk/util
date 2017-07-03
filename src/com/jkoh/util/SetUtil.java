package com.jkoh.util;

import java.util.*;

public class SetUtil {
	public enum SetType {
		HASHSET { public <E> Set<E> newSet() { return new HashSet<E>(); } },
		LINKEDHASHSET { public <E> Set<E> newSet() { return new LinkedHashSet<E>(); } },
		TREESET { public <E> Set<E> newSet() { return new TreeSet<E>(); } 
			public <E> Set<E> newSet(Comparator<? super E> comparator) { return new TreeSet<E>(comparator); } };
		public abstract <E> Set<E> newSet();
		public <E> Set<E> newSet(Comparator<? super E> comparator) { throw new UnsupportedOperationException("Comparator not supported for " + this); }
	}
	public enum NavigableSetType {
		TREESET { public <E> NavigableSet<E> newNavigableSet() { return new TreeSet<E>(); } 
			public <E> NavigableSet<E> newNavigableSet(Comparator<? super E> comparator) { return new TreeSet<E>(comparator); } };
		public abstract <E> NavigableSet<E> newNavigableSet();
		public abstract <E> NavigableSet<E> newNavigableSet(Comparator<? super E> comparator);
	}
}