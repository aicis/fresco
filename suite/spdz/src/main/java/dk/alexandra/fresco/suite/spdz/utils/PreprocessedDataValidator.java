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
package dk.alexandra.fresco.suite.spdz.utils;

/**
 * Checks that the different components of the preprocessed data is correct.
 * That is, that no malformed triples, inputmasks etc. exists.
 * 
 * NB: CURRENTLY DOES NOT WORK. NEEDS TO CREATE SPDZ STORAGE FROM SCRATCH AND
 * GET PREPRO DATA FROM THAT. DO NOT TRY TO FETCH FROM SCE.
 * 
 * @author Kasper Damgaard
 *
 */
public class PreprocessedDataValidator {

	private static String dataPath;
	private static int numberOfPlayers;
	private static int numberOfTriples;
	private static int numberOfInputMasks;
	private static int numberOfExpPipes;
	private static int numberOfBits;

	private static final int ARGS_BEFORE_ADDR = 3 + 4;

	public static void main(String[] args) {
		boolean single;
		String[] remoteAddrs;

		if (args.length < ARGS_BEFORE_ADDR) {
			usage();
			return;
		} else {
			try {
				single = Boolean.parseBoolean(args[2]);
			} catch (Exception e) {
				usage();
				return;
			}
			if (!single) {
				if (args.length <= ARGS_BEFORE_ADDR) {
					usage();
					return;
				}
			}
		}
		numberOfPlayers = Integer.parseInt(args[1]);
		dataPath = args[0];
		numberOfTriples = Integer.parseInt(args[3]);
		numberOfInputMasks = Integer.parseInt(args[4]);
		numberOfBits = Integer.parseInt(args[5]);
		numberOfExpPipes = Integer.parseInt(args[6]);

		if (!single) {
			remoteAddrs = new String[args.length - ARGS_BEFORE_ADDR];
			for (int i = ARGS_BEFORE_ADDR; i < args.length; i++) {
				remoteAddrs[i] = args[i];
			}
			multipleWay(remoteAddrs);
		} else {
			singleWay();
		}
	}
	
	private static void singleWay() {
		throw new RuntimeException("Not implemented yet");
	}
/*
  private static void singleWay() {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {

						BigInteger[] outputs1 = new BigInteger[numberOfTriples];
						BigInteger[] outputs2 = new BigInteger[numberOfTriples];
						BigInteger[] outputs3 = new BigInteger[numberOfTriples];

						ParallelGateProducer par = new ParallelGateProducer();
						// Check triples
						System.out.println("Starting checks of triples");
						for (int i = 0; i < numberOfTriples; i++) {
							SpdzTriple triple = supplier.getNextTriple();
							outputs1[i] = new SpdzOInt();
							outputs2[i] = new SpdzOInt();
							outputs3[i] = new SpdzOInt();
							SpdzOutputToAllGate o1 = new SpdzOutputToAllGate(
									new SpdzSInt(triple.getA()), outputs1[i]);
							SpdzOutputToAllGate o2 = new SpdzOutputToAllGate(
									new SpdzSInt(triple.getB()), outputs2[i]);
							SpdzOutputToAllGate o3 = new SpdzOutputToAllGate(
									new SpdzSInt(triple.getC()), outputs3[i]);
							par.append(o1);
							par.append(o2);
							par.append(o3);
						}

						evaluator.eval(par);
						for (int i = 0; i < numberOfTriples; i++) {
							BigInteger ab = outputs1[i].getValue()
									.multiply(outputs2[i].getValue())
									.mod(Util.getModulus());
							if (!Util.convertRepresentation(ab).equals(
									outputs3[i].getValue())) {
								System.out.println("Triple no. " + i
										+ " was not correct");
							}
						}
						System.out.println("Done checking triples");
						System.out.println("Starting checks of Input masks");
						// Check input masks
						for (int towardsPlayerId = 1; towardsPlayerId <= numberOfPlayers; towardsPlayerId++) {
							BigInteger[] values = new BigInteger[numberOfInputMasks];
							BigInteger[] outputs = new BigInteger[numberOfInputMasks];
							par = new ParallelGateProducer();
							for (int i = 0; i < numberOfInputMasks; i++) {
								SpdzInputMask mask = supplier
										.getNextInputMask(towardsPlayerId);
								if (conf.getMyId() == towardsPlayerId) {
									values[i] = mask.getRealValue();
								}
								outputs[i] = new SpdzOInt();
								SpdzSInt number = new SpdzSInt(mask.getMask());
								SpdzOutputToAllGate o1 = new SpdzOutputToAllGate(
										number, outputs[i]);
								par.append(o1);
							}
							evaluator.eval(par);
							for (int i = 0; i < numberOfInputMasks; i++) {
								if (conf.getMyId() == towardsPlayerId) {
									if (!Util.convertRepresentation(values[i])
											.equals(outputs[i].getValue())) {
										System.out.println("Input no. " + i
												+ " was not correct");
									}
								}
							}
							System.out.println("Checked masks towards player "
									+ towardsPlayerId);
						}
						System.out.println("Done checking Input masks");
						System.out.println("Starting checks of Bits");
						// Check bits
						for (int i = 0; i < numberOfBits; i++) {
							SpdzSInt bit = supplier.getNextBit();

							SpdzOInt output1 = new SpdzOInt();
							SpdzOutputToAllGate o1 = new SpdzOutputToAllGate(
									bit, output1);
							SequentialProtocolProducer prod = new SequentialProtocolProducer(
									o1);

							evaluator.eval(prod);

							if (!output1.value.equals(BigInteger.ONE)
									&& !output1.value.equals(BigInteger.ZERO)) {
								System.out.println("Bit no " + i
										+ " was not correct");
							}
						}

						System.out.println("Done checking Bits");
						System.out.println("Starting checks of ExpPipes");
						// Check expPipe
						for (int i = 0; i < numberOfExpPipes; i++) {
							SpdzSInt[] expPipe = supplier.getNextExpPipe();
							if (!checkExpCircuit(evaluator, expPipe)) {
								System.out.println("Exp Pipe no " + i
										+ " was not correct");
							}
						}
						System.out.println("Done checking ExpPipes");
						network.shutdown();
					}
				};
			}
		}, numberOfPlayers);
	}

	private static boolean checkExpCircuit(ProtocolEvaluator evaluator,
			SInt[] outputs) {
		BigInteger[] openedVals = new BigInteger[outputs.length];
		for (int i = 0; i < outputs.length; i++) {
			SpdzOInt output1 = new SpdzOInt();
			SpdzOutputToAllGate o1 = new SpdzOutputToAllGate(outputs[i],
					output1);
			SequentialProtocolProducer prod = new SequentialProtocolProducer(o1);

			evaluator.eval(prod);
			openedVals[i] = output1.getValue();
		}
		BigInteger R = openedVals[1];
		BigInteger nextR = R;
		BigInteger R_inv = openedVals[0];
		if (!BigInteger.ONE.equals(R.multiply(R_inv).mod(Util.getModulus()))) {
			return false;
		}
		for (int i = 2; i < outputs.length; i++) {
			nextR = R.multiply(nextR).mod(Util.getModulus());
			if (!Util.convertRepresentation(nextR).equals(openedVals[i])) {
				return false;
			}
		}
		BigInteger[] Ms = Util.getClearExpPipe(R);
		for (int i = 0; i < Ms.length; i++) {
			if (!Util.convertRepresentation(Ms[i]).equals(openedVals[i + 1])) {
				return false;
			}
		}
		return true;
	}
*/
	private static void multipleWay(String[] addrs) {
		throw new RuntimeException("Not implemented yet");
	}
	

	public static void usage() {
		System.out.println("Usage:");
		System.out
				.println("Validator does validation of preprocessed data. To do so, you must supply it with:");
		System.out.println("dataPath=*A path to the preprocessed data*");
		System.out
				.println("numberOfPlayers=*The amount of players in the Spdz Setup - must also be specified even if single=true*");
		System.out
				.println("single=*Boolean for running this on a single machine (requires all data to be at the data path) or between multiple machines*");
		System.out
				.println("numberOfTriples=*The amount of triples that you would like to check.");
		System.out
				.println("numberOfInputMasks=*The amount of Input masks that you would like to check.");
		System.out
				.println("numberOfBits=*The amount of Bits that you would like to check.");
		System.out
				.println("numberOfExpPipes=*The amount of Exp pipes that you would like to check.");
		System.out
				.println("If single=false, you must specify the remote addresses and ports of the other parties (as e.g. 127.0.0.1:8080)");
		System.out.println("Thus, a valid run would look something like:");
		System.out
				.println("java -jar preprocessedDataValidator.jar triples/spdz2_byte 2 true 1000000 100000 10000 1000");
	}
}
