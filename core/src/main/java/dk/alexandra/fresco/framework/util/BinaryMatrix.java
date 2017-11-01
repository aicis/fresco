package dk.alexandra.fresco.framework.util;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

/**
 * Instances of this class represents binary matrices, eg. 0-1 matrices.
 * <p>
 * Note that this implementation is optimized for reading columns, and it uses a {@link BitSet} for
 * the underlying representation of the entries in the matrix.
 * </p>
 */
public class BinaryMatrix {

  private BitSet bits;
  private int height;
  private int width;

  /**
   * Create a new matrix with <i>m</i> rows and <i>n</i> columns and all entries set to
   * <code>false</code>.
   *
   * @param n
   * @param m
   */
  public BinaryMatrix(int m, int n) {
    this(m, n, (m > 0 && n > 0) ? new BitSet(n * m) : null);
  }

  /**
   * Create a new BinaryMatrix with height <i>m</i>, width <i>n</i> and the specified BitSet
   * representation. See {@link #getIndex} on how this representation is interpreted.
   *
   * @param m
   * @param n
   * @param bits
   */
  private BinaryMatrix(int m, int n, BitSet bits) {
    if (m > 0 && n > 0) {
      this.height = m;
      this.width = n;
      this.bits = bits;
    } else {
      throw new IllegalArgumentException("Matrix dimensions must be strictly positive but was "
          + m + " x " + n + ".");
    }
  }

  public BinaryMatrix(byte[] data) {
    ByteBuffer buf = ByteBuffer.wrap(data);
    this.height = Short.toUnsignedInt(buf.getShort());
    this.width = Short.toUnsignedInt(buf.getShort());
    this.bits = BitSet.valueOf(buf);
  }

  /**
   * Returns the (<i>i,j</i>)'th entry of this matrix.
   *
   * @param i
   * @param j
   * @return
   */
  public boolean get(int i, int j) {
    return bits.get(getIndex(i, j));
  }

  /**
   * We store bits column-wise, so the (<i>i,j</i>)'th entry is at position <i>j * m + i</i> where
   * <i>m</i> is the height of the matrix.
   *
   * @param i
   * @param j
   * @return
   */
  private int getIndex(int i, int j) {
    if ((i < 0 || i >= height) || (j < 0 || j >= width)) {
      throw new IndexOutOfBoundsException("Trying to access index (" + i + ", " + j
          + ") in matrix of dimension " + height + " x " + width);
    } else {
      return j * getHeight() + i;
    }
  }

  /**
   * Set the (<i>i,j</i>)'th entry of this matrix to be <i>b</i>.
   *
   * @param i
   * @param j
   * @param b
   */
  public void set(int i, int j, boolean b) {
    this.bits.set(getIndex(i, j), b);
  }

  /**
   * Returns the width of this matrix.
   *
   * @return
   */
  public int getWidth() {
    return width;
  }

  /**
   * Returns the height of this matrix.
   *
   * @return
   */
  public int getHeight() {
    return height;
  }

  /**
   * Return the <i>i</i>'th row of this matrix.
   *
   * @param i
   * @return
   */
  public BitSet getRow(int i) {
    BitSet r = new BitSet(getWidth());
    for (int j = 0; j < getWidth(); j++) {
      r.set(j, this.get(i, j));
    }
    return r;
  }

  /**
   * Return a new matrix which is formed of a subset of this matrix as specified by the given int
   * array.
   *
   * @param rows
   * @return
   */
  public BinaryMatrix getRows(int[] rows) {
    BinaryMatrix r = new BinaryMatrix(rows.length, getWidth());
    for (int i = 0; i < rows.length; i++) {
      r.setRow(i, this.getRow(rows[i]));
    }
    return r;
  }

  /**
   * Replace the <i>i</i>'th row of this matrix with <i>v</i>.
   *
   * @param i
   * @param v
   */
  public void setRow(int i, BitSet v) {
    for (int j = 0; j < getWidth(); j++) {
      this.set(i, j, v.get(j));
    }
  }

  /**
   * Return the <i>j</i>'th column of this matrix.
   *
   * @param j
   * @return
   */
  public BitSet getColumn(int j) {
    return bits.get(getIndex(0, j), getIndex(height - 1, j) + 1);
  }

  /**
   * Return a new matrix which is formed by a subset of the columns of this matrix as specified and
   * ordered in the given int-array.
   *
   * @param columns
   * @return
   */
  public BinaryMatrix getColumns(int[] columns) {
    BinaryMatrix c = new BinaryMatrix(getHeight(), columns.length);
    for (int j = 0; j < columns.length; j++) {
      c.setColumn(j, this.getColumn(columns[j]));
    }
    return c;
  }

  /**
   * Replace the <i>j</i>'th column with the given bits.
   *
   * @param j
   * @param v
   */
  public void setColumn(int j, BitSet v) {
    for (int i = 0; i < getHeight(); i++) {
      set(i, j, v.get(i));
    }
  }

  /**
   * Set column <i>j</i> to be all zeroes.
   *
   * @param j
   */
  public void clearColumn(int j) {
    bits.clear(getIndex(0, j), getIndex(0, j + 1));
  }

  /**
   * Return the transpose of this matrix.
   */
  public BinaryMatrix transpose() {
    BinaryMatrix matrix = new BinaryMatrix(getWidth(), getHeight());
    for (int i = 0; i < getWidth(); i++) {
      for (int j = 0; j < getHeight(); j++) {
        matrix.set(i, j, get(j, i));
      }
    }
    return matrix;
  }

  /**
   * Add the given matrix to <code>this</code> entry-wise.
   *
   * @param a
   */
  public void add(BinaryMatrix a) {
    if (a.getHeight() != getHeight() || a.getWidth() != getWidth()) {
      throw new IllegalArgumentException("Matrices must have same size");
    }
    bits.xor(a.bits);
  }

  /**
   * Multiply this matrix onto the vector <i>v</i>.
   *
   * @param v
   * @return
   */
  public BitSet multiply(BitSet v) {
    BitSet result = new BitSet(getHeight());
    for (int i = 0; i < getHeight(); i++) {
      result.set(i, BitSetUtils.innerProduct(getRow(i), v));
    }
    return result;
  }

  /**
   * Return a new matrix which is the result of this times <i>b</i>.
   *
   * @param b
   * @return
   */
  public BinaryMatrix multiply(BinaryMatrix b) {
    if (b.getHeight() != this.getWidth()) {
      throw new IllegalArgumentException("Cannot multiply matrices of mismatching dimensions.");
    }
    BinaryMatrix result = new BinaryMatrix(this.getHeight(), b.getWidth());
    for (int i = 0; i < result.getHeight(); i++) {
      BitSet row = getRow(i);
      for (int j = 0; j < result.getWidth(); j++) {
        result.set(i, j, BitSetUtils.innerProduct(row, b.getColumn(j)));
      }
    }
    return result;
  }

  /**
   * Given two vectors <i>v,w</i> of length <i>m, n</i> resp., this method returns the
   * <a href="https://en.wikipedia.org/wiki/Outer_product">outer product</a> of the two.
   *
   * <p>
   *  Note we represent vectors using BitSets and if these do not have the expected size
   *  they will be padded with zeroes or extra bits will be disregarded respectively.
   * </p>
   *
   * @param m height of the resulting matrix
   * @parem n width of the resulting matrix
   * @param v a BitSet representing a vector of <i>m</i> bits
   * @param w a BitSet representing a vector of <i>n</i> bits
   * @return Matrix of size <i>m &times; n</i>.
   *
   */
  public static BinaryMatrix outerProduct(int m, int n, BitSet v, BitSet w) {
    BinaryMatrix a = new BinaryMatrix(m, n);
    for (int i = 0; i < m; i++) {
      if (v.get(i)) {
        a.setRow(i, w);
      }
    }
    return a;
  }

  /**
   * Return a matrix of size <i>m &times; n</i> with random entries chosen using the
   * {@link Random#nextBoolean()} method of the given {@link Random} instance.
   *
   * @param m
   * @param n
   * @param random
   * @return
   */
  public static BinaryMatrix getRandomMatrix(int m, int n, Random random) {
    return new BinaryMatrix(m, n, BitSetUtils.getRandomBits(n * m, random));
  }

  public static BinaryMatrix fromColumns(List<BitSet> columns, int height) {
    BinaryMatrix matrix = new BinaryMatrix(height, columns.size());
    for (int j = 0; j < columns.size(); j++) {
      matrix.setColumn(j, columns.get(j));
    }
    return matrix;
  }

  public byte[] toByteArray() {
    byte[] data = bits.toByteArray();
    byte[] m_arr = ByteBuffer.allocate(2).putShort((short) height).array();
    byte[] n_arr = ByteBuffer.allocate(2).putShort((short) width).array();
    byte[] res = new byte[2 + 2 + data.length];
    System.arraycopy(m_arr, 0, res, 0, 2);
    System.arraycopy(n_arr, 0, res, 2, 2);
    System.arraycopy(data, 0, res, 4, data.length);
    return res;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < height; i++) {
      sb.append(BitSetUtils.toString(getRow(i), width));
      sb.append("\n");
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (o.getClass() != getClass()) {
      return false;
    }
    BinaryMatrix other = (BinaryMatrix) o;

    if (getWidth() != other.getWidth() || getHeight() != other.getHeight()) {
      return false;
    }
    return bits.equals(other.bits);
  }
}
