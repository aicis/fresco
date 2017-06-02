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
package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;
import dk.alexandra.fresco.suite.spdz.utils.Util;
import java.math.BigInteger;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestFakeTripGen {

	@Test
	public void testTripleGen() {
		int amount = 100000;
		int noOfParties = 2;
		BigInteger modulus = new BigInteger("6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
		BigInteger alpha = new BigInteger("5081587041441179438932635098620319894716368628029284292880408086703438041331200877980213770035569812296677935118715454650749402237663859711459266577679205");
		Util.setModulus(modulus);
		List<SpdzTriple[]> triples = FakeTripGen.generateTriples(amount, noOfParties, modulus, alpha);
		for(SpdzTriple[] t : triples) {
			BigInteger a = t[0].getA().getShare().add(t[1].getA().getShare()).mod(modulus);
			BigInteger b = t[0].getB().getShare().add(t[1].getB().getShare()).mod(modulus);
			BigInteger c = t[0].getC().getShare().add(t[1].getC().getShare()).mod(modulus);
			
			BigInteger shareA = t[0].getA().getMac().add(t[1].getA().getMac()).mod(modulus);
			BigInteger shareB = t[0].getB().getMac().add(t[1].getB().getMac()).mod(modulus);
			BigInteger shareC = t[0].getC().getMac().add(t[1].getC().getMac()).mod(modulus);
			
			Assert.assertEquals(c, a.multiply(b).mod(modulus));
			
			Assert.assertEquals(BigInteger.ZERO, shareA.subtract(a.multiply(alpha).mod(modulus)));
			Assert.assertEquals(BigInteger.ZERO, shareB.subtract(b.multiply(alpha).mod(modulus)));
			Assert.assertEquals(BigInteger.ZERO, shareC.subtract(c.multiply(alpha).mod(modulus)));
		}
	}
	
	@Test
	public void testInputMasks() {
		int amount = 100000;
		int noOfParties = 2;
		BigInteger modulus = new BigInteger("6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
		BigInteger alpha = new BigInteger("5081587041441179438932635098620319894716368628029284292880408086703438041331200877980213770035569812296677935118715454650749402237663859711459266577679205");
		Util.setModulus(modulus);
		List<List<SpdzInputMask[]>> inps = FakeTripGen.generateInputMasks(amount, noOfParties, modulus, alpha);
		for(int towardsPlayer = 1; towardsPlayer < noOfParties+1; towardsPlayer++) {
			List<SpdzInputMask[]> inputMasks = inps.get(towardsPlayer-1);
			for(SpdzInputMask[] masks : inputMasks) {
				Assert.assertNotNull(masks[towardsPlayer-1].getRealValue());
			}
		}
	}
}
