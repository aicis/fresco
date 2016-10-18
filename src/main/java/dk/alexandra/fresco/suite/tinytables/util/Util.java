package dk.alexandra.fresco.suite.tinytables.util;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class Util {
	
	/**
	 * Given a sorted map with integer keys and entries of type <code>T[]</code>
	 * , this method returns a list of all entries of type <code>T</code> in the
	 * induced ordering.
	 * 
	 * @param map
	 * @return
	 */
	public static <T> List<T> getAll(SortedMap<Integer, T[]> map) {
		List<T> array = new ArrayList<T>();
		for (int i : map.keySet()) {
			T[] entry = map.get(i);
			for (T t : entry) {
				array.add(t);
			}
		}
		return array;
	}
	
}
