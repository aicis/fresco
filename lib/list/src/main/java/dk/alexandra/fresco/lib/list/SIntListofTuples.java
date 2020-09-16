package dk.alexandra.fresco.lib.list;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for a tuple of data points and the duplicate indicators.
 */ 
public class SIntListofTuples {

  private List<List<DRes<SInt>>> theData = new ArrayList<>();
  private List<DRes<SInt>> duplicateP = new ArrayList<>();

  /**
   * id is first element in tuple.
   */ 
  public final int rowWidth;

  /**
   * @param row the data values (and id in first column)
   * @param falseValue an Sint representing zero, to mark the row as non-duplicate.
   */ 
  public SIntListofTuples add(List<DRes<SInt>> row, DRes<SInt> falseValue) {
    if (row.size() != rowWidth) {
      throw new RuntimeException("Row width wrong. Should be " + rowWidth);
    }
    theData.add(row);
    duplicateP.add(falseValue);
    return this;
  }

  public DRes<SInt> getId(int index) {
    return theData.get(index).get(0);
  }

  public SIntListofTuples(int rowWidth) {
    this.rowWidth = rowWidth;
  }

  public void setDuplicate(int index, DRes<SInt> value) {
    duplicateP.set(index, value);
  }

  public DRes<SInt> getDuplicate(int index) {
    return duplicateP.get(index);
  }

  public int size() {
    return theData.size();
  }

}