// package dk.alexandra.fresco.lib.collections.permute;
//
// import java.math.BigInteger;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
//
// import dk.alexandra.fresco.framework.Computation;
// import dk.alexandra.fresco.framework.ProtocolProducer;
// import dk.alexandra.fresco.framework.builder.ComputationBuilder;
// import dk.alexandra.fresco.framework.builder.ComputationBuilderParallel;
// import dk.alexandra.fresco.framework.builder.NumericBuilder;
// import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.ParallelNumericBuilder;
// import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
// import dk.alexandra.fresco.framework.value.SInt;
// import dk.alexandra.fresco.lib.collections.Matrix;
// import dk.alexandra.fresco.lib.collections.io.CloseMatrix;
// import dk.alexandra.fresco.lib.conditional.ConditionalSwapRows;
// import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
// import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
// import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
// import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
//
//// TODO: for malicious security add check that control bits are in fact bits
// public class PermuteRows implements ComputationBuilder<Matrix<Computation<SInt>>> {
//
// private Matrix<Computation<SInt>> values;
// private Matrix<Computation<SInt>> cbits;
// private int[] idxPerm;
// private int permProviderPid;
// private int n;
// private int elsPerRow;
// private boolean isPermProvider;
// private int numRounds;
// private WaksmanUtils wutils;
//
// // public PermuteRows(List<SInt[]> values, int[] idxPerm, int permProviderPid,
// // BasicNumericFactory bnf) {
// // this(values, idxPerm, permProviderPid, true, false, bnf);
// // }
// //
// // public PermuteRows(List<SInt[]> values, int permProviderPid, BasicNumericFactory bnf) {
// // this(values, null, permProviderPid, false, false, bnf);
// // }
//
// public PermuteRows(Matrix<Computation<SInt>> values, int[] idxPerm, int permProviderPid,
// boolean isPermProvider) {
// super();
// this.values = values;
// this.n = values.getHeight();
// this.elsPerRow = values.getWidth();
// this.idxPerm = idxPerm;
// this.isPermProvider = isPermProvider;
// this.permProviderPid = permProviderPid;
// this.wutils = new WaksmanUtils();
// }
//
// private List<SInt[]> doRound(List<SInt[]> roundInputs, SInt[][] cbits, int numRows, int numCols,
// int colIdx) {
// SInt[][] roundOutputs = new SInt[numRows][elsPerRow];
//
// swapNeighs(cbits, colIdx);
//
// boolean inward = (colIdx < (int) numCols / 2);
// if (!inward) {
// colIdx = (numCols / 2) - colIdx % (numCols / 2) - 1;
// }
// int numPerms = (int) Math.pow(2, colIdx + 1);
// int elsPerPerm = (int) numRows / numPerms;
// for (int permPairIdx = 0; permPairIdx < numPerms - 1; permPairIdx += 2) {
// int topPermStart = permPairIdx * elsPerPerm;
// int bottomPermStart = (permPairIdx + 1) * elsPerPerm;
//
// int nextFreeTop = topPermStart;
// int nextFreeBottom = bottomPermStart;
// if (inward) {
// for (int i = 0; i < elsPerPerm * 2 - 1; i += 2) {
// int inputIdx = topPermStart + i;
// roundOutputs[nextFreeTop++] = roundInputs.get(inputIdx);
// roundOutputs[nextFreeBottom++] = roundInputs.get(inputIdx + 1);
// }
// } else {
// for (int i = 0; i < elsPerPerm * 2 - 1; i += 2) {
// int inputIdx = topPermStart + i;
// roundOutputs[inputIdx] = roundInputs.get(nextFreeTop++);
// roundOutputs[inputIdx + 1] = roundInputs.get(nextFreeBottom++);
// }
// }
// }
// return new ArrayList<SInt[]>(Arrays.asList(roundOutputs));
// }
//
// // private void swapNeighs(SInt[][] cbits, int colIdx) {
// // npb.beginParScope();
// // for (int i = 0; i < values.size() - 1; i += 2) {
// // npb.conditionalSwap(cbits[i / 2][colIdx], values.get(i), values.get(i + 1));
// // }
// // npb.endCurScope();
// // }
//
// // @Override
// // public ProtocolProducer nextProtocolProducer() {
// // if (round == 0) {
// // /*
// // * Handle empty case
// // */
// // if (n == 0) {
// // return null;
// // }
// // /*
// // * We are in the first round so we need to set and secret-share the control bits
// // */
// // int numRows = wutils.getNumRowsRequired(n);
// // int numCols = wutils.getNumColsRequired(n);
// //
// // numRounds = numCols;
// // if (numRounds == 0) {
// // return null;
// // }
// //
// // if (isPermProvider) {
// // int[][] _cbits = wutils.setControlBits(idxPerm);
// // cbits = niob.inputMatrix(_cbits, permProviderPid);
// // } else {
// // cbits = niob.inputMatrix(numRows, numCols, permProviderPid);
// // }
// // round++;
// // return niob.getProtocol();
// // } else if (round < numRounds) {
// // /*
// // * We are in an intermediate round so we need to apply the next column of swappers and route
// // * the outputs for the next round
// // */
// // int numCols = cbits[0].length;
// // int numRows = values.size();
// // values = doRound(values, cbits, numRows, numCols, round - 1);
// // round++;
// // return npb.getProtocol();
// // } else if (round == numRounds) {
// // /*
// // * We are in the last round so we apply the last column of swappers
// // */
// // swapNeighs(cbits, numRounds - 1);
// // round++;
// // return npb.getProtocol();
// // } else {
// // return null;
// // }
// // }
//
// @Override
// public Computation<Matrix<Computation<SInt>>> build(SequentialNumericBuilder builder) {
// // Determine dimensions of waksman network
// final int numRows = wutils.getNumRowsRequired(n);
// final int numCols = wutils.getNumColsRequired(n);
// final int numRounds = numCols;
//
// return builder.seq(seq -> {
// // TODO: handle empty case
//
// // We are in the first round so we need to set and secret-share the control bits
//
// // numRounds = numCols;
// // if (numRounds == 0) {
// // return null;
// // }
//
// if (isPermProvider) {
// return seq
// .createParallelSub(new CloseMatrix(wutils.setControlBits(idxPerm), permProviderPid));
// } else {
// return seq.createParallelSub(new CloseMatrix(numRows, numCols, permProviderPid));
// }
// }).seq((bits, seq) -> {
// // set control bits
// cbits = bits;
// // initiate loop
// return new IterationState(0, () -> values);
// }).whileLoop((state) -> state.round < numRounds, (state, par) -> {
// // swap neighs
// // re-arrange for next iteration
// System.out.println(cbits);
// return new IterationState(state.round + 1, () -> cbits);
// }).seq((state, seq) -> {
// return state.intermediate;
// });
// }
//
// private static final class IterationState implements Computation<IterationState> {
//
// private final int round;
// private final Computation<Matrix<Computation<SInt>>> intermediate;
//
// private IterationState(int round, Computation<Matrix<Computation<SInt>>> intermediate) {
// this.round = round;
// this.intermediate = intermediate;
// }
//
// @Override
// public IterationState out() {
// return this;
// }
// }
// }
