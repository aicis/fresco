package dk.alexandra.fresco.lib.list;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mortenvchristiansen
 */ 
public class SIntListofTuples {

  private List<List<Computation<SInt>>> theData = new ArrayList<>();
  private List<Computation<SInt>> duplicate_p = new ArrayList<>();

  /**
   * id is first element in tuple
   */ 
  public final int rowWidth;

  /**
   * @param row the data values (and id in first column)
   * @param falseValue an Sint representing zero, to mark the row as non-duplicate.
   */ 
  public SIntListofTuples add(List<Computation<SInt>> row, Computation<SInt> falseValue) {
    if (row.size() != rowWidth) {
      throw new RuntimeException("Row width wrong. Should be " + rowWidth);
    }
    theData.add(row);
    duplicate_p.add(falseValue);
    return this;
  }

  public SIntListofTuples remove(int index) {
    theData.remove(index);
    duplicate_p.remove(index);
    return this;
  }

  public List<Computation<SInt>> get(int index) {
    return theData.get(index);
  }

  public Computation<SInt> getId(int index) {
    return theData.get(index).get(0);
  }

  public SIntListofTuples(int rowWidth) {
    this.rowWidth = rowWidth;
  }

  public void setDuplicate(int index, Computation<SInt> value) {
    duplicate_p.set(index, value);
  }

  public Computation<SInt> getDuplicate(int index) {
    return duplicate_p.get(index);
  }

  public int size() {
    return theData.size();
  }

}