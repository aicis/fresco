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
package dk.alexandra.fresco.lib.collections.sort;

import java.util.List;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

/**
 * An implementation of the OddEvenMergeProtocol. This does not support threading,
 * the OddEvenMergeProtocolRec class should be preferable to this.
 * 
 * @author psn
 *
 */
public class OddEvenMergeProtocolImpl extends AbstractSimpleProtocol implements
		OddEvenMergeProtocol {

	private BasicLogicBuilder blb;
	private List<Pair<SBool[], SBool[]>> sorted;
	private List<Pair<SBool[], SBool[]>> left;
	private List<Pair<SBool[], SBool[]>> right;
	private int firstIndex;
	private int lastIndex;
	private int realSize;
	private int simulatedSize;

	public OddEvenMergeProtocolImpl(List<Pair<SBool[], SBool[]>> left,
			List<Pair<SBool[], SBool[]>> right,
			List<Pair<SBool[], SBool[]>> sorted, AbstractBinaryFactory factory) {
		super();
		this.blb = new BasicLogicBuilder(factory);
		this.sorted = sorted;
		this.left = left;
		this.right = right;
	}

	@Override
	protected ProtocolProducer initializeProtocolProducer() {
		
		blb.beginParScope();
		for (int i = 0; i < left.size(); i++) {
			Pair<SBool[], SBool[]> leftPair = left.get(i);
			Pair<SBool[], SBool[]> upperPair = sorted.get(i);
			blb.copy(leftPair.getFirst(), upperPair.getFirst());
			blb.copy(leftPair.getSecond(), upperPair.getSecond());
		}
		for (int i = 0; i < right.size(); i++) {
			Pair<SBool[], SBool[]> rightPair = right.get(i);
			Pair<SBool[], SBool[]> lowerPair = sorted.get(i + left.size());
			blb.copy(rightPair.getFirst(), lowerPair.getFirst());
			blb.copy(rightPair.getSecond(), lowerPair.getSecond());
		}
		blb.endCurScope();
		initializeIndices();
		newMerge(0, simulatedSize, 1);
		return blb.getProtocol();
	}

	private void merge(int first, int length, int step) {
		int doubleStep = step * 2;
		if (length > 2) {
			blb.beginSeqScope();
			blb.beginParScope();
			int newLength = length / 2;
			merge(first, newLength, doubleStep);
			newMerge(first + step, length - newLength, doubleStep);
			//merge(first + step, length - newLength, doubleStep);
			blb.endCurScope();
			blb.beginParScope();
			for (int i = 1; i < length - 2; i += 2) {
				int low = first + i * step;
				int high = low + step;
				compareAndSwapAtIndices(low, high);
			}
			blb.endCurScope();
			blb.endCurScope();
		} else if (length == 2) {
			compareAndSwapAtIndices(first, first + step);
		}		
	}
	
	private void newMerge(int first, int length, int step) {
		blb.beginSeqScope();
		merge(first, length, step);
		blb.endCurScope();
	}

	private void initializeIndices() {
		int leftPad = 0;
		int rightPad = 0;
		int leftSize = left.size();
		int rightSize = right.size();
		int difference = leftSize - rightSize;
		if (difference > 0) {
			rightPad += difference;
		} else {
			leftPad -= difference;
		}
		realSize = left.size() + right.size();
		simulatedSize = 1;
		while (simulatedSize < (realSize + leftPad + rightPad)) {
			simulatedSize = simulatedSize << 1;
		}
		int halfSize = simulatedSize >>> 1;
		leftPad += halfSize - (leftPad + leftSize);
		rightPad += halfSize - (rightPad + rightSize);
		firstIndex = leftPad;
		lastIndex = simulatedSize - rightPad - 1;
	}

	private void compareAndSwapAtIndices(int i, int j) {
		boolean inBounds = (i >= firstIndex && i < lastIndex);
		inBounds = inBounds && (j >= firstIndex && j <= lastIndex);
		if (!inBounds) {
			return;
		} else {			
			i = i - firstIndex;
			j = j - firstIndex;
			Pair<SBool[], SBool[]> left = sorted.get(i);
			Pair<SBool[], SBool[]> right = sorted.get(j);
			blb.keyedCompareAndSwap(left.getFirst(), left.getSecond(),
					right.getFirst(), right.getSecond());
		}
	}

	public static int getTriplesUsedForLength(int totalSize, int indexSize, int seqSize) {
		int k = (int) (Math.log(totalSize)/Math.log(2));	
		if((int)Math.pow(2, k) < totalSize){
			k++;
		}
		int triplesUsed = k*totalSize*(indexSize + 2*(indexSize+seqSize));
		triplesUsed = 0;
		for(int i = k; i > 0; i--){
			int layer = getTriplesForLayer(i, totalSize/i, k, indexSize, seqSize);
			triplesUsed += layer;
		}
		return triplesUsed;
	}
	
	private static int getTriplesForLayer(int i, int l, int k, int indexSize, int seqSize){
		if(i == 1){
			//return (l/2) * (indexSize+(indexSize+seqSize));
			return (l/2) * (indexSize+2*(indexSize+seqSize));
		}
		int i2 = (int) Math.pow(2, i);
		int li = (i2-2)/2;
		int ki2 = (int)Math.pow(2, k-i);
		//TODO: This should not be right, but gives apperently a good estimate. Should be:
		return li* ki2 * (indexSize+2*(indexSize+seqSize));
		//return li* ki2 * (indexSize+(indexSize+seqSize));
	}
}
