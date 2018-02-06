package dk.alexandra.fresco.suite.tinytables.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.BitSet;
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
	
	/**
	 * Outputs a hash of j and the given bits of size l. We assume that l < 256
	 * since the underlying hash function is SHA-256.
	 * 
	 * @param j
	 * @param bits
	 * @param l
	 * @return
	 */
	public static BitSet hash(int j, BitSet bits, int l) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		digest.update((byte) j);
		byte[] binary = digest.digest(bits.toByteArray());
		return BitSet.valueOf(binary).get(0, l);
	}
	
	public static int otherPlayerId(int myId) {
		return myId == 1 ? 2 : 1;
	}
}
