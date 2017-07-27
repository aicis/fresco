package dk.alexandra.fresco.lib.list;

import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;

public class TestSIntListOfTuples {

  @Test (expected=IllegalArgumentException.class)
  public void testLengthCheck() {
    SIntListofTuples list = new SIntListofTuples(5);
    SInt[] tuple = new SInt[2];
    list.add(tuple, null);
    Assert.fail("Should not be reachable.");
  }
  
  
  @Test
  public void testGetReadOnlyListAndDelete() {
    SIntListofTuples list = new SIntListofTuples(2);
    SInt[] tuple = new SInt[] {new DummyArithmeticSInt(2), new DummyArithmeticSInt(2)};
    SInt val1 = new DummyArithmeticSInt(2);
    list.add(tuple, val1);
    
    SInt[] tuple2 = new SInt[] {new DummyArithmeticSInt(3), new DummyArithmeticSInt(3)};
    SInt val2 = new DummyArithmeticSInt(3);
    list.add(tuple2, val2);
    
    
    List<SInt[]> readOnlyList = list.getReadOnlyList();
    
    Assert.assertThat(readOnlyList.get(0)[0], Is.is(new DummyArithmeticSInt(2)));
    Assert.assertThat(readOnlyList.get(0)[0], Is.is(list.get(0)[0]));
    Assert.assertThat(readOnlyList.get(1)[0], Is.is(new DummyArithmeticSInt(3)));
    list.remove(0);
    readOnlyList = list.getReadOnlyList();
    Assert.assertThat(readOnlyList.size(), Is.is(1));
    Assert.assertThat(readOnlyList.get(0)[0], Is.is(new DummyArithmeticSInt(3)));
  }
}
