package dk.alexandra.fresco.lib.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class Matrix<T> {

  private final int width;
  private final int height;
  private final ArrayList<ArrayList<T>> matrix;

  /**
   * Creates an matrix from a row building function.
   * 
   * @param height height of the matrix
   * @param width width of the matrix
   * @param rowBuilder the function for building rows
   */
  public Matrix(int height, int width, IntFunction<ArrayList<T>> rowBuilder) {
    this.width = width;
    this.matrix = new ArrayList<>(height);
    this.height = height;
    for (int i = 0; i < height; i++) {
      this.matrix.add(rowBuilder.apply(i));
    }
  }

  /**
   * Clones matrix.
   * 
   * @param other
   */
  public Matrix(Matrix<T> other) {
    this.width = other.getWidth();
    this.height = other.getHeight();
    this.matrix = other.getRows().stream().map(row -> new ArrayList<>(row))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Creates a matrix directly from an ArrayList of ArrayLists.
   * 
   * @param height height of the matrix
   * @param width width of the matrix
   * @param matrix the array data
   */
  public Matrix(int height, int width, ArrayList<ArrayList<T>> matrix) {
    this.width = width;
    this.height = height;
    this.matrix = matrix;
  }

  /**
   * Gets all rows of the matrix.
   * 
   * Convenience method for iterating over the rows of the matrix.
   * 
   * @return
   */
  public ArrayList<ArrayList<T>> getRows() {
    return this.matrix;
  }

  public ArrayList<T> getRow(int i) {
    return matrix.get(i);
  }

  public void setRow(int i, ArrayList<T> row) {
    matrix.set(i, row);
  }

  /**
   * Gets the width of the matrix.
   * 
   * @return the width of the matrix
   */
  public int getWidth() {
    return width;
  }


  /**
   * Gets the height of the matrix.
   * 
   * @return the height of the matrix
   */
  public int getHeight() {
    return height;
  }

  public List<T> getColumn(int i) {
    return this.matrix.stream().map(row -> row.get(i)).collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "Matrix{" + "width=" + width + ", height=" + height + ", matrix=" + matrix + '}';
  }

}
