/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
 *******************************************************************************/
package dk.alexandra.fresco.lib.lp;

import java.util.Arrays;

/**
 * @author psn
 *
 * @param <T>
 */
public class Matrix<T> {
	
	private final T[][] matrix;
	
	/**
	 * Internal matrix representation:
	 * [*, *, *]
	 * [*, *, *]
	 * [*, *, *]
	 * [*, *, *]
	 * 
	 * here you would give as length of input to get this matrix:
	 * [4][3]
	 * @param matrix
	 */
	public Matrix(T[][] matrix){
		this.matrix = matrix;
	}
	
	public T[][] getDoubleArray() {
		return matrix;
	}
	
	public T getElement(int row, int column){
		return this.matrix[row][column];
	}
	
	/**
	 * 
	 * @param i index of the row you wish to extract.
	 * @return
	 */
	public T[] getIthRow(int i){
		return this.matrix[i];
	}
	
	/**
	 * Placeholder needed since we cannot create new instances of generic array, 
	 * and Java arrays are not flexible.
	 * @param i index of the column you want
	 * @param placeholder an array of correct generic type of length=#rows in this matrix.
	 * @return
	 */
	public T[] getIthColumn(int i, T[] placeholder){
		if(placeholder.length != this.matrix.length){
			throw new RuntimeException("placeholder length differs from column length of internal matrix.");
		}
		for(int j = 0; j < placeholder.length; j++){
			placeholder[j] = this.matrix[j][i]; 
		}
		return placeholder;
	}
	
	
	/**
	 * @return the width of the matrix
	 */
	public int getWidth() {
		return this.matrix[0].length;
	}
	
	
	/**
	 * @return the height of the matrix
	 */
	public int getHeight() {
		return this.matrix.length;
	}
	
	@Override
	public String toString(){
		String s = "";
		for(int i = 0; i < this.matrix.length; i++){
			s+=Arrays.toString(this.matrix[i])+"\n";
		}
		return s;
	}
	
}
