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
package dk.alexandra.fresco.lib.math.integer.log;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import org.junit.Assert;


/**
 * Generic test cases for basic finite field operations.
 * 
 * Can be reused by a test case for any protocol suite that implements the basic
 * field protocol factory.
 *
 */
public class LogTests {

	public static class TestLogarithm extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {
				private final BigInteger[] x = { BigInteger.valueOf(201235), BigInteger.valueOf(1234), BigInteger.valueOf(405068), BigInteger.valueOf(123456), BigInteger.valueOf(110) };
        private final ArrayList<Computation<BigInteger>> results = new ArrayList<>(x.length);

				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						@Override
						public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
							return ProtocolBuilder
									.createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    NumericBuilder sIntFactory = builder.numeric();

										for (BigInteger input : x) {
											Computation<SInt> actualInput = sIntFactory.known(input);
											Computation<SInt> result = builder.createAdvancedNumericBuilder()
													.log(actualInput, input.bitLength());
                      Computation<BigInteger> openResult = builder.numeric().open(result);
                      results.add(openResult);
                    }
									}).build();
						}
					};
					secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          for (int i = 0; i < x.length; i++) {
            int actual = results.get(i).out().intValue();
            int expected = (int) Math.log(x[i].doubleValue());
            int difference = Math.abs(actual - expected);
            Assert.assertTrue(difference <= 1); // Difference should be less than a bit
          }
				}
			};
		}
	}
}
