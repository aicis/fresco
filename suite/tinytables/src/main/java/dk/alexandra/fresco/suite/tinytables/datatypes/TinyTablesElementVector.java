package dk.alexandra.fresco.suite.tinytables.datatypes;

import dk.alexandra.fresco.framework.util.RegularBitVector;
import java.io.Serializable;


/**
 * This class implements a storage optimised way of keeping TinyTablesElements. Here each is
 * represented only by it's share in a {@link RegularBitVector}. So if using {@link #setShare},
 * {@link #get} on the same index will not return the same TinyTablesElement but simply a
 * TinyTablesElement with the same share.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesElementVector implements Serializable {

  private static final long serialVersionUID = -2405648771000699453L;
  private final RegularBitVector values;

  public TinyTablesElementVector(byte[] shares, int size) {
    this.values = new RegularBitVector(shares, size);
  }

  public TinyTablesElementVector(int size) {
    this.values = new RegularBitVector(size);
  }

  public void setShare(int index, boolean share) {
    this.values.setBit(index, share);
  }

  public TinyTablesElement get(int index) {
    return new TinyTablesElement(values.getBit(index));
  }

  public byte[] payload() {
    return this.values.toByteArray();
  }

  public int getSize() {
    return values.getSize();
  }

  public static RegularBitVector open(TinyTablesElementVector... vectors) {
    RegularBitVector values = new RegularBitVector(vectors[0].getSize());
    for (TinyTablesElementVector vector : vectors) {
      values.xor(vector.values);
    }
    return values;
  }

}
