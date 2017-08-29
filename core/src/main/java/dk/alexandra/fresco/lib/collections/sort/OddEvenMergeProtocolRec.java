/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import java.util.List;

/**
 * An implementation of the OddEvenMergeProtocol. This implementation supports threading. It first
 * recursively generates a list for each layer of the sorting network of all indicies that must be
 * compared and swapped in that layer.
 *
 * This implementation solves the problem of the lengths of inputs not being equal and/or a two
 * power by padding the inputs with imaginary dummy elements that are either infinitely big or
 * infinitely small. When a compare-and-swap operation with one such dummy element should be done,
 * we just skip the operation as the result is already known.
 *
 * No SBools are constructed for the imaginary padding elements, so they should not take up
 * additional memory.
 *
 * @author psn
 */
public class OddEvenMergeProtocolRec extends SimpleProtocolProducer
    implements OddEvenMergeProtocol {

  private final List<Pair<SBool[], SBool[]>> sorted;
  private List<Pair<SBool[], SBool[]>> left;
  private List<Pair<SBool[], SBool[]>> right;
  private final int firstIndex;
  private final int lastIndex;
  private final int realSize;
  private final int simulatedSize;
  private int layers;
  // private List<Layer> layerList;

  /**
   * Constructs the protocol merging two lists of key/value pairs. The lists are assumed to be
   * sorted on the key.
   *
   * @param left a list of key/value pairs.
   * @param right a list of key/value pairs.
   * @param sorted an list to hold the resulting (merged) sorted list of key/value pairs.
   * @param factory an AbstractBinaryFactory
   */
  public OddEvenMergeProtocolRec(List<Pair<SBool[], SBool[]>> left,
      List<Pair<SBool[], SBool[]>> right, List<Pair<SBool[], SBool[]>> sorted) {
    super();
    this.sorted = sorted;
    this.left = left;
    this.right = right;
    // Compute indices to simulate padding to even size input of two power
    // length
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
    int tmpSimulatedSize = 1;
    layers = 0;
    while (tmpSimulatedSize < (realSize + leftPad + rightPad)) {
      layers++;
      tmpSimulatedSize = tmpSimulatedSize << 1;
    }
    int halfSize = tmpSimulatedSize >>> 1;
    leftPad += halfSize - (leftPad + leftSize);
    rightPad += halfSize - (rightPad + rightSize);
    firstIndex = leftPad;
    lastIndex = tmpSimulatedSize - rightPad - 1;
    simulatedSize = tmpSimulatedSize;
  }

  @Override
  protected ProtocolProducer initializeProtocolProducer() {
    /*
     * BasicLogicBuilder blb = new BasicLogicBuilder(factory); blb.beginSeqScope();
     * List<ProtocolLayer> clList = getProtocolProducersForThreads(); for (ProtocolLayer cl :
     * clList) { blb.beginParScope(); for (ProtocolProducer pp : cl) { blb.addProtocolProducer(pp);
     * } blb.endCurScope(); } blb.endCurScope(); return blb.getProtocol();
     */ return null;
  }

  /**
   * A recursive call to compute the layers involved in sorting a given sublist. The sublist defined
   * by its first index in the underlying array, the length of the list and the distance (step)
   * between each element in the sublist in the underlying array.
   *
   * This will construct a list of sublists that should be handled in each layer.
   *
   * @param first the first index of the sublist.
   * @param length the length of the sublist
   * @param step the step of the sublist (distance between elements of the sublist)
   */
  private void recurse(int first, int length, int step) {
    int newLength = length / 2;
    int doubleStep = step * 2;
    if (length > 2) {
      recurse(first, newLength, doubleStep);
      recurse(first + step, length - newLength, doubleStep);
    }
    int index = 0;
    int tmpLength = length;
    tmpLength >>>= 1;
    while (tmpLength > 1) {
      tmpLength >>>= 1;
      index++;
    }
    // layerList.get(index).addIndex(first);
  }

//  private List<ProtocolLayer> getProtocolProducersForThreads() {
    // BasicLogicBuilder blb = new BasicLogicBuilder(factory);
//    List<ProtocolLayer> protocolLayers = new ArrayList<ProtocolLayer>(layers + 1);
    // Copy input to output array
    /*
     * blb.beginParScope(); for (int i = 0; i < left.size(); i++) { Pair<SBool[], SBool[]> leftPair
     * = left.get(i); Pair<SBool[], SBool[]> upperPair = sorted.get(i);
     * blb.copy(leftPair.getFirst(), upperPair.getFirst()); blb.copy(leftPair.getSecond(),
     * upperPair.getSecond()); } for (int i = 0; i < right.size(); i++) { Pair<SBool[], SBool[]>
     * rightPair = right.get(i); Pair<SBool[], SBool[]> lowerPair = sorted.get(i + left.size());
     * blb.copy(rightPair.getFirst(), lowerPair.getFirst()); blb.copy(rightPair.getSecond(),
     * lowerPair.getSecond()); } blb.endCurScope(); ProtocolLayer firstLayer = new ProtocolLayer(1);
     * firstLayer.add(blb.getProtocol()); protocolLayers.add(firstLayer); // Compute indices int
     * length = 2; int step = simulatedSize >>> 1; this.layerList = new ArrayList<Layer>(layers);
     * layerList.add(new Layer(length, step)); while (length < simulatedSize) { length = length <<
     * 1; step = step >>> 1; layerList.add(new Layer(length, step)); } // No need to hold these
     * anymore left = null; right = null;
     * 
     * // Recurse to generate layers recurse(0, simulatedSize, 1); for (Layer l : layerList) {
     * protocolLayers.add(l.getProtocolLayer()); }
     */
//    return protocolLayers;
//  }

  /**
   * A protocol compares and swaps a list of elements. All compare and swap operations are done in
   * parallel.
   *
   * @author psn
   */
  private class SwapList extends SimpleProtocolProducer {

    List<Pair<Integer, Integer>> swapList;

    public SwapList(List<Pair<Integer, Integer>> swapList) {
      super();
      this.swapList = swapList;
    }

    @Override
    protected ProtocolProducer initializeProtocolProducer() {
      ParallelProtocolProducer par = new ParallelProtocolProducer();
      for (Pair<Integer, Integer> swap : swapList) {
        ProtocolProducer pp = compareAndSwapAtIndices(swap.getFirst(), swap.getSecond());
        if (pp != null) {
          par.append(pp);
        }
      }
      return par;
    }

    private ProtocolProducer compareAndSwapAtIndices(int i, int j) {
      boolean outOfBounds = false;
      if (i < firstIndex || i >= lastIndex) {
        outOfBounds = true;
      }
      if (j < firstIndex || j > lastIndex) {
        outOfBounds = true;
      }
      if (outOfBounds) {
        return null;
      }
      i = i - firstIndex;
      j = j - firstIndex;
      Pair<SBool[], SBool[]> left = sorted.get(i);
      Pair<SBool[], SBool[]> right = sorted.get(j);
      // return factory.getKeyedCompareAndSwapProtocol(left.getFirst(),
      // left.getSecond(), right.getFirst(), right.getSecond());
      return null;
    }
  }

  /**
   * A protocol representing all the swap operations that must be done in a single layer. Supports
   * threading by generating a protocol layer where the work in split up in a number of independent
   * protocols. At present the split is set somewhat arbitrarily to 16.
   *
   * @author psn
   *//*
     * private class Layer {//extends SimpleProtocolProducer {
     * 
     * int length; int step; List<Integer> indices; BasicLogicBuilder builder;
     * 
     * protected Layer(int length, int step) { this.length = length; this.step = step; this.indices
     * = new LinkedList<Integer>(); this.builder = new BasicLogicBuilder(factory); }
     * 
     * protected void addIndex(int i) { indices.add(i); }
     */
  /*
   * @Override protected ProtocolProducer initializeProtocolProducer() { builder.beginParScope();
   * ProtocolLayer cl = getProtocolLayer(); for (ProtocolProducer pp : cl) {
   * builder.addProtocolProducer(pp); } builder.endCurScope(); return builder.getProtocol(); }
   */ /*
      * public ProtocolLayer getProtocolLayer() { int threads = 16; // TODO: Find a smarter way to
      * set this!! ProtocolLayer cl = new ProtocolLayer(threads); int numSwaps = (indices.size() *
      * (length - 2)) / 2; if (length == 2) { numSwaps = indices.size(); } int swapsPerThread =
      * (numSwaps / threads) + 1; List<Pair<Integer, Integer>> swapList = new
      * ArrayList<Pair<Integer, Integer>>( swapsPerThread); for (int first : indices) { if (length
      * == 2) { swapList.add(new Pair<Integer, Integer>(first, first + step)); if (swapList.size()
      * == swapsPerThread) { cl.add(new SwapList(swapList)); swapList = new ArrayList<Pair<Integer,
      * Integer>>( swapsPerThread); } } else { for (int i = 1; i < length - 2; i += 2) { int low =
      * first + i * step; int high = low + step; swapList.add(new Pair<Integer, Integer>(low,
      * high)); if (swapList.size() == swapsPerThread) { cl.add(new SwapList(swapList)); swapList =
      * new ArrayList<Pair<Integer, Integer>>( swapsPerThread); } } } } if (swapList.size() != 0) {
      * cl.add(new SwapList(swapList)); } return cl; } }
      */
}
