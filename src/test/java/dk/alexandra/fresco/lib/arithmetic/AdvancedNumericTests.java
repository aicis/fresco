/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.sce.SCE;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import org.junit.Assert;

import java.io.IOException;
import java.math.BigInteger;

public class AdvancedNumericTests {

    public static class TestDivision extends TestThreadRunner.TestThreadFactory {

        private int numerator;
        private int denominator;

        public TestDivision(int numerator, int denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }

        @Override
        public TestThreadRunner.TestThread next(TestThreadRunner.TestThreadConfiguration conf) {
            return new ThreadWithFixture() {
                @Override
                public void test() throws Exception {
                    TestApplication app = new TestApplication() {
                        @Override
                        public ProtocolProducer prepareApplication(ProtocolFactory factory) {
                            OmniBuilder builder = new OmniBuilder(factory);
                            NumericIOBuilder io = builder.getNumericIOBuilder();
                            AdvancedNumericBuilder advanced = builder.getAdvancedNumericBuilder();

                            SInt p = io.input(numerator, 1);
                            SInt q = io.input(denominator, 1);
                            SInt result = advanced.div(p, q);

                            outputs = new OInt[] { io.output(result) };

                            return builder.getProtocol();
                        }
                    };

                    sce.runApplication(app);

                    Assert.assertEquals(BigInteger.valueOf(numerator / denominator),
                            app.getOutputs()[0].getValue());
                }
            };
        }
    }

    public static class TestDivisionWithPrecision extends TestThreadRunner.TestThreadFactory {

        @Override
        public TestThreadRunner.TestThread next(TestThreadRunner.TestThreadConfiguration conf) {
            return new ThreadWithFixture() {
                @Override
                public void test() throws Exception {
                    TestApplication app = new TestApplication() {
                        @Override
                        public ProtocolProducer prepareApplication(ProtocolFactory factory) {
                            OmniBuilder builder = new OmniBuilder(factory);
                            NumericIOBuilder io = builder.getNumericIOBuilder();
                            NumericProtocolBuilder numeric = builder.getNumericProtocolBuilder();
                            AdvancedNumericBuilder advanced = builder.getAdvancedNumericBuilder();

                            SInt p = io.input(9, 1);
                            SInt q = io.input(4, 1);
                            OInt precision = numeric.knownOInt(4);
                            SInt result = advanced.div(p, q, precision);

                            outputs = new OInt[] { io.output(result) };

                            return builder.getProtocol();
                        }
                    };

                    sce.runApplication(app);

                    Assert.assertEquals(BigInteger.valueOf(9 / 4),
                            app.getOutputs()[0].getValue());
                }
            };
        }
    }

    public static class TestDivisionWithKnownDenominator extends TestThreadRunner.TestThreadFactory {

        @Override
        public TestThreadRunner.TestThread next(TestThreadRunner.TestThreadConfiguration conf) {
            return new ThreadWithFixture() {
                @Override
                public void test() throws Exception {
                    TestApplication app = new TestApplication() {
                        @Override
                        public ProtocolProducer prepareApplication(ProtocolFactory factory) {
                            OmniBuilder builder = new OmniBuilder(factory);
                            NumericIOBuilder io = builder.getNumericIOBuilder();
                            NumericProtocolBuilder numeric = builder.getNumericProtocolBuilder();
                            AdvancedNumericBuilder advanced = builder.getAdvancedNumericBuilder();

                            SInt p = io.input(8, 1);
                            OInt q = numeric.knownOInt(4);
                            SInt result = advanced.div(p, 4, q);

                            outputs = new OInt[] { io.output(result) };

                            return builder.getProtocol();
                        }
                    };

                    sce.runApplication(app);

                    Assert.assertEquals(BigInteger.valueOf(8 / 4),
                            app.getOutputs()[0].getValue());
                }
            };
        }
    }

    public static class TestDivisionWithRemainder extends TestThreadRunner.TestThreadFactory {

        @Override
        public TestThreadRunner.TestThread next(TestThreadRunner.TestThreadConfiguration conf) {
            return new ThreadWithFixture() {
                @Override
                public void test() throws Exception {
                    TestApplication app = new TestApplication() {
                        @Override
                        public ProtocolProducer prepareApplication(ProtocolFactory factory) {
                            OmniBuilder builder = new OmniBuilder(factory);
                            NumericIOBuilder io = builder.getNumericIOBuilder();
                            NumericProtocolBuilder numeric = builder.getNumericProtocolBuilder();
                            AdvancedNumericBuilder advanced = builder.getAdvancedNumericBuilder();

                            SInt p = io.input(9, 1);
                            OInt q = numeric.knownOInt(4);
                            SInt[] results = advanced.divWithRemainder(p, 4, q);

                            outputs = io.outputArray(results);

                            return builder.getProtocol();
                        }
                    };

                    sce.runApplication(app);

                    Assert.assertEquals(BigInteger.valueOf(9 / 4),
                            app.getOutputs()[0].getValue());
                    Assert.assertEquals(BigInteger.valueOf(9 % 4),
                            app.getOutputs()[1].getValue());
                }
            };
        }
    }

    public static class TestModulus extends TestThreadRunner.TestThreadFactory {

        @Override
        public TestThreadRunner.TestThread next(TestThreadRunner.TestThreadConfiguration conf) {
            return new ThreadWithFixture() {
                @Override
                public void test() throws Exception {
                    TestApplication app = new TestApplication() {
                        @Override
                        public ProtocolProducer prepareApplication(ProtocolFactory factory) {
                            OmniBuilder builder = new OmniBuilder(factory);
                            NumericIOBuilder io = builder.getNumericIOBuilder();
                            NumericProtocolBuilder numeric = builder.getNumericProtocolBuilder();
                            AdvancedNumericBuilder advanced = builder.getAdvancedNumericBuilder();

                            SInt p = io.input(9, 1);
                            OInt q = numeric.knownOInt(4);
                            SInt result = advanced.mod(p, 4, q);

                            outputs = new OInt[] { io.output(result) };

                            return builder.getProtocol();
                        }
                    };

                    sce.runApplication(app);

                    Assert.assertEquals(BigInteger.valueOf(9 % 4),
                            app.getOutputs()[0].getValue());
                }
            };
        }
    }

    private abstract static class ThreadWithFixture extends TestThreadRunner.TestThread {

        protected SCE sce;

        @Override
        public void setUp() throws IOException {
            sce = SCEFactory.getSCEFromConfiguration(conf.sceConf,
                    conf.protocolSuiteConf);
        }

    }
}
