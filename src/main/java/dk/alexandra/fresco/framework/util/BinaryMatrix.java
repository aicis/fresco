package dk.alexandra.fresco.framework.util;

import java.io.Serializable;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

/**
 * Instances of this class represents binary matrices, eg. 0-1 matrices.
 * 
 * Note that this implementation is optimized for reading columns, and it uses a
 * {@link BitSet} for the underlying representation of the entries in the
 * matrix.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class BinaryMatrix implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4936656911598727916L;
	private BitSet bits;
	private int m, n;

	/**
	 * Create a new matrix with <i>m</i> rows and <i>n</i> columns and all
	 * entries set to <code>false</code>.
	 * 
	 * @param n
	 * @param m
	 */
	public BinaryMatrix(int m, int n) {
		this(m, n, new BitSet(n * m));
	}

	/**
	 * Create a new BinaryMatrix with height <i>m</i>, width <i>n</i> and the
	 * specified BitSet representation. See {@link #getIndex} on how this
	 * representation is interpreted.
	 * 
	 * @param m
	 * @param n
	 * @param bits
	 */
	private BinaryMatrix(int m, int n, BitSet bits) {
		this.m = m;
		this.n = n;
		this.bits = bits;
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
	 * We store bits column-wise, so the (<i>i,j</i>)'th entry is at position
	 * <i>j * m + i</i> where <i>m</i> is the height of the matrix.
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private int getIndex(int i, int j) {
		return j * getHeight() + i;
	}

	/**
	 * Set the (<i>i,j</i>)'th entry of this matrix to be <i>b</i>.
	 * 
	 * @param i
	 * @param j
	 * @param value
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
		return n;
	}

	/**
	 * Returns the height of this matrix.
	 * 
	 * @return
	 */
	public int getHeight() {
		return m;
	}

	/**
	 * Return a new matrix which is formed of a subset of this matrix as
	 * specified by the given int array.
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
	 * Return the <i>j</i>'th column of this matrix.
	 * 
	 * @param j
	 * @return
	 */
	public BitSet getColumn(int j) {
		return bits.get(getIndex(0, j), getIndex(0, j + 1));
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
	 * Return a new matrix which is formed by a subset of the columns of this
	 * matrix as specified and ordered in the given int-array.
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
	 * Return a new matrix which is the result of this times <i>b</i>.
	 * 
	 * @param b
	 * @return
	 */
	public BinaryMatrix multiply(BinaryMatrix b) {
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
	 * Return a matrix of size <i>m &times; n</i> with random entries chosen
	 * using the {@link Random#nextBoolean()} method of the given {@link Random}
	 * instance.
	 * 
	 * @param m
	 * @param n
	 * @param random
	 * @return
	 */
	public static BinaryMatrix getRandomMatrix(int m, int n, Random random) {
		return new BinaryMatrix(m, n, BitSetUtils.getRandomBits(n * m, random));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m; i++) {
			sb.append(BitSetUtils.toString(getRow(i), n));
			sb.append("\n");
		}
		return sb.toString();
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
	 * Given two vectors <i>v,w</i> of length <i>m, n</i> resp., this method
	 * returns the <a href="https://en.wikipedia.org/wiki/Outer_product">outer
	 * product</a> of the two.
	 * 
	 * @param v
	 *            Vector of length <i>m</i>.
	 * @param w
	 *            Vector of length <i>n</i>.
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
	
	public static BinaryMatrix fromColumns(List<BitSet> columns, int height) {
		BinaryMatrix matrix = new BinaryMatrix(height, columns.size());
		for (int j = 0; j < columns.size(); j++) {
			matrix.setColumn(j, columns.get(j));
		}
		return matrix;
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
