package dk.alexandra.fresco.lib.collections;

import dk.alexandra.fresco.framework.DRes;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ListUtilsTest {
    @Test
    public void unwrapsElementsOfList() {
        DRes<List<DRes<Integer>>> wrapped = () -> asList(() -> 1, () -> 2);
        assertEquals(ListUtils.unwrap(wrapped), asList(1, 2));
    }
}
