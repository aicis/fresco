package dk.alexandra.fresco.lib.collections;

import dk.alexandra.fresco.framework.DRes;

import java.util.List;
import java.util.stream.Collectors;

public class ListUtils {

    private ListUtils() {
        // should not be instantiated
    }

    /**
     * Unwraps the elements of the list.
     */
    public static <T, L extends List<DRes<T>>> List<T> unwrap(DRes<L> list) {
        return list.out().stream().map(DRes::out).collect(Collectors.toList());
    }
}
