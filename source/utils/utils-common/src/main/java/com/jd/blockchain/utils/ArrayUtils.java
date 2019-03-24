package com.jd.blockchain.utils;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author haiq
 *
 */
public abstract class ArrayUtils {
	private ArrayUtils() {
	}
	
	public static <T> T[] singleton(T obj, Class<T> clazz) {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(clazz, 1);
		array[0] = obj;
		return array;
	}
	
	public static <T> T[] toArray(Iterator<T> itr, Class<T> clazz){
		List<T> lst = new LinkedList<T>();
		while (itr.hasNext()) {
			T t = (T) itr.next();
			lst.add(t);
		}
		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(clazz, lst.size());
		lst.toArray(array);
		return array;
	}
	
	public static <T> List<T> asList(T[] array){
		return asList(array, 0, array.length);
	}
	
	public static <T> Set<T> asSet(T[] array){
		if (array == null || array.length == 0) {
			return Collections.emptySet();
		}
		HashSet<T> set = new HashSet<T>();
		for (T t : array) {
			set.add(t);
		}
		return set;
	}
	
	public static <T> SortedSet<T> asSortedSet(T[] array){
		if (array == null || array.length == 0) {
			return Collections.emptySortedSet();
		}
		TreeSet<T> set = new TreeSet<T>();
		for (T t : array) {
			set.add(t);
		}
		return set;
	}
	
	public static <T> List<T> asList(T[] array, int fromIndex){
		return asList(array, fromIndex, array.length);
	}
	
	public static <T> List<T> asList(T[] array, int fromIndex, int toIndex){
		if (toIndex < fromIndex) {
			throw new IllegalArgumentException("The toIndex less than fromIndex!");
		}
		if (fromIndex < 0) {
			throw new IllegalArgumentException("The fromIndex is negative!");
		}
		if (toIndex > array.length) {
			throw new IllegalArgumentException("The toIndex great than the length of array!");
		}
		
		if (fromIndex == toIndex) {
			return Collections.emptyList();
		}
		return new ReadonlyArrayListWrapper<T>(array, fromIndex, toIndex);
	}

}
