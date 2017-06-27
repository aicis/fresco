/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 */
package dk.alexandra.fresco.lib.lp;

import java.util.ArrayList;

/**
 * @author psn
 */
public class Matrix4<T> {

  private final ArrayList<ArrayList<T>> matrix;
  private final int width;
  private final int height;

  /**
   * Internal matrix representation:
   * [*, *, *]
   * [*, *, *]
   * [*, *, *]
   * [*, *, *]
   *
   * here you would give as length of input to get this matrix:
   * [4][3]
   */
  public Matrix4(int width, int height) {
    this.width = width;
    this.height = height;
    this.matrix = new ArrayList<>(height);
  }


  public T getElement(int row, int column) {
    boundsCheck(row, column);
    return getElement(getRow(row), column);
  }

  private T getElement(ArrayList<T> row, int column) {
    if (column >= row.size()) {
      return null;
    } else {
      return row.get(column);
    }
  }

  private void boundsCheck(int row, int column) {
    boundsCheckRow(row);
    boundsCheckColumn(column);
  }

  private void boundsCheckColumn(int column) {
    if (column >= width) {
      throw new IndexOutOfBoundsException();
    }
  }

  private void boundsCheckRow(int row) {
    if (row >= height) {
      throw new IndexOutOfBoundsException();
    }
  }

  private ArrayList<T> getRow(int row) {
    while (row >= this.matrix.size()) {
      this.matrix.add(new ArrayList<>(width));
    }

    return this.matrix.get(row);
  }

  /**
   * @return the width of the matrix
   */
  public int getWidth() {
    return width;
  }


  /**
   * @return the height of the matrix
   */
  public int getHeight() {
    return height;
  }
}
