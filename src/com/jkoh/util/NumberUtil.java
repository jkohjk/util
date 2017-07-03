package com.jkoh.util;

import java.util.*;

public class NumberUtil {
	public interface NumberOperators<T extends Number> {
		public T cast(Object number);
		public T add(Number number1, Number number2);
		public T sub(Number number1, Number number2);
		public T mul(Number number1, Number number2);
		public T div(Number number1, Number number2);
		public T mod(Number number1, Number number2);
	}
	public static final Map<Class<? extends Number>, NumberOperators<? extends Number>> numberOperators = generateNumberOperators();
	private static Map<Class<? extends Number>, NumberOperators<? extends Number>> generateNumberOperators() {
		Map<Class<? extends Number>, NumberOperators<? extends Number>> numOps = new HashMap<>();
		numOps.put(Integer.class, new NumberOperators<Integer>() {
			public Integer cast(Object number) { 
				return number instanceof Number ? ((Number)number).intValue() : Integer.valueOf(number.toString());
			}
			public Integer add(Number number1, Number number2) { return cast(number1) + cast(number2); }
			public Integer sub(Number number1, Number number2) { return cast(number1) - cast(number2); }
			public Integer mul(Number number1, Number number2) { return cast(number1) * cast(number2); }
			public Integer div(Number number1, Number number2) { return cast(number1) / cast(number2); }
			public Integer mod(Number number1, Number number2) { return cast(number1) % cast(number2); }
		});
		numOps.put(int.class, numOps.get(Integer.class));
		numOps.put(Long.class, new NumberOperators<Long>() {
			public Long cast(Object number) { 
				return number instanceof Number ? ((Number)number).longValue() : Long.valueOf(number.toString());
			}
			public Long add(Number number1, Number number2) { return cast(number1) + cast(number2); }
			public Long sub(Number number1, Number number2) { return cast(number1) - cast(number2); }
			public Long mul(Number number1, Number number2) { return cast(number1) * cast(number2); }
			public Long div(Number number1, Number number2) { return cast(number1) / cast(number2); }
			public Long mod(Number number1, Number number2) { return cast(number1) % cast(number2); }
		});
		numOps.put(long.class, numOps.get(Long.class));
		numOps.put(Float.class, new NumberOperators<Float>() {
			public Float cast(Object number) {
				return number instanceof Number ? ((Number)number).floatValue() : Float.valueOf(number.toString());
			}
			public Float add(Number number1, Number number2) { return cast(number1) + cast(number2); }
			public Float sub(Number number1, Number number2) { return cast(number1) - cast(number2); }
			public Float mul(Number number1, Number number2) { return cast(number1) * cast(number2); }
			public Float div(Number number1, Number number2) { return cast(number1) / cast(number2); }
			public Float mod(Number number1, Number number2) { return cast(number1) % cast(number2); }
		});
		numOps.put(float.class, numOps.get(Float.class));
		numOps.put(Double.class, new NumberOperators<Double>() {
			public Double cast(Object number) {
				return number instanceof Number ? ((Number)number).doubleValue() : Double.valueOf(number.toString());
			}
			public Double add(Number number1, Number number2) { return cast(number1) + cast(number2); }
			public Double sub(Number number1, Number number2) { return cast(number1) - cast(number2); }
			public Double mul(Number number1, Number number2) { return cast(number1) * cast(number2); }
			public Double div(Number number1, Number number2) { return cast(number1) / cast(number2); }
			public Double mod(Number number1, Number number2) { return cast(number1) % cast(number2); }
		});
		numOps.put(double.class, numOps.get(Double.class));
		return Collections.unmodifiableMap(numOps);
	}
	public static final Map<Class<? extends Number>, Class<? extends Number>> primitiveToWrapper = generatePrimitiveToWrapper();
	private static Map<Class<? extends Number>, Class<? extends Number>> generatePrimitiveToWrapper() {
		Map<Class<? extends Number>, Class<? extends Number>> primToWrap = new HashMap<>();
		primToWrap.put(int.class, Integer.class);
		primToWrap.put(long.class, Long.class);
		primToWrap.put(float.class, Float.class);
		primToWrap.put(double.class, Double.class);
		return Collections.unmodifiableMap(primToWrap);
	}
	public static final Map<Class<? extends Number>, Integer> numberTypeHierarchy = generateNumberTypeHierarchy();
	private static Map<Class<? extends Number>, Integer> generateNumberTypeHierarchy() {
		Map<Class<? extends Number>, Integer> hierarchy = new HashMap<>();
		hierarchy.put(Integer.class, 1);
		hierarchy.put(Long.class, 2);
		hierarchy.put(Float.class, 3);
		hierarchy.put(Double.class, 4);
		return Collections.unmodifiableMap(hierarchy);
	}
	public static Class<? extends Number> getHighestHierarchyType(Number ... numbers) {
		Class<? extends Number> highestHierarchy = null;
		for(Number number : numbers) {
			if(numberTypeHierarchy.containsKey(number.getClass())) {
				if(highestHierarchy == null || numberTypeHierarchy.get(number.getClass()) > numberTypeHierarchy.get(highestHierarchy)) {
					highestHierarchy = number.getClass();
				}
			}
		}
		return highestHierarchy;
	}
	
	public static <T extends Number> T cast(Object number, Class<T> type) {
		if(numberOperators.containsKey(type)) {
			return (T)numberOperators.get(type).cast(number);
		} else {
			throw new IllegalArgumentException(type.getName() + " not supported");
		}
	}
	public static Number add(Number number1, Number number2) {
		Class<? extends Number> type = getHighestHierarchyType(number1, number2);
		if(type != null && numberOperators.containsKey(type)) {
			return numberOperators.get(type).add(number1, number2);
		} else {
			throw new IllegalArgumentException("Input Number types not supported");
		}
	}
	public static <T extends Number> T add(Number number1, Number number2, Class<T> type) {
		if(numberOperators.containsKey(type)) {
			return (T)numberOperators.get(type).add(number1, number2);
		} else {
			throw new IllegalArgumentException(type.getName() + " not supported");
		}
	}
	public static <T extends Number> T addAndCast(Number number1, Number number2, Class<T> type) {
		return cast(add(number1, number2), type);
	}
	public static Number sub(Number number1, Number number2) {
		Class<? extends Number> type = getHighestHierarchyType(number1, number2);
		if(type != null && numberOperators.containsKey(type)) {
			return numberOperators.get(type).sub(number1, number2);
		} else {
			throw new IllegalArgumentException("Input Number types not supported");
		}
	}
	public static <T extends Number> T sub(Number number1, Number number2, Class<T> type) {
		if(numberOperators.containsKey(type)) {
			return (T)numberOperators.get(type).sub(number1, number2);
		} else {
			throw new IllegalArgumentException(type.getName() + " not supported");
		}
	}
	public static <T extends Number> T subAndCast(Number number1, Number number2, Class<T> type) {
		return cast(sub(number1, number2), type);
	}
	public static Number mul(Number number1, Number number2) {
		Class<? extends Number> type = getHighestHierarchyType(number1, number2);
		if(type != null && numberOperators.containsKey(type)) {
			return numberOperators.get(type).mul(number1, number2);
		} else {
			throw new IllegalArgumentException("Input Number types not supported");
		}
	}
	public static <T extends Number> T mul(Number number1, Number number2, Class<T> type) {
		if(numberOperators.containsKey(type)) {
			return (T)numberOperators.get(type).mul(number1, number2);
		} else {
			throw new IllegalArgumentException(type.getName() + " not supported");
		}
	}
	public static <T extends Number> T mulAndCast(Number number1, Number number2, Class<T> type) {
		return cast(mul(number1, number2), type);
	}
	public static Number div(Number number1, Number number2) {
		Class<? extends Number> type = getHighestHierarchyType(number1, number2);
		if(type != null && numberOperators.containsKey(type)) {
			return numberOperators.get(type).div(number1, number2);
		} else {
			throw new IllegalArgumentException("Input Number types not supported");
		}
	}
	public static <T extends Number> T div(Number number1, Number number2, Class<T> type) {
		if(numberOperators.containsKey(type)) {
			return (T)numberOperators.get(type).div(number1, number2);
		} else {
			throw new IllegalArgumentException(type.getName() + " not supported");
		}
	}
	public static <T extends Number> T divAndCast(Number number1, Number number2, Class<T> type) {
		return cast(div(number1, number2), type);
	}
	public static Number mod(Number number1, Number number2) {
		Class<? extends Number> type = getHighestHierarchyType(number1, number2);
		if(type != null && numberOperators.containsKey(type)) {
			return numberOperators.get(type).mod(number1, number2);
		} else {
			throw new IllegalArgumentException("Input Number types not supported");
		}
	}
	public static <T extends Number> T mod(Number number1, Number number2, Class<T> type) {
		if(numberOperators.containsKey(type)) {
			return (T)numberOperators.get(type).mod(number1, number2);
		} else {
			throw new IllegalArgumentException(type.getName() + " not supported");
		}
	}
	public static <T extends Number> T modAndCast(Number number1, Number number2, Class<T> type) {
		return cast(mod(number1, number2), type);
	}
}