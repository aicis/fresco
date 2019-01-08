package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.TestFrameworkException;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;

import static dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy.SEQUENTIAL;

public class DummyArithmeticRunner {
    static public <T> T run(Application<T, ProtocolBuilderNumeric> application) {
        return run(application, 1);
    }

    static public <T> T run(Application<T, ProtocolBuilderNumeric> application, int numberOfParties) {
        try {
            // run application inside test framework
            return new TestFramework<T>(numberOfParties).run(application);
        } catch (TestFrameworkException exception) {
            // strip test framework exceptions to get to the actual exception
            if (exception.getCause().getCause() instanceof RuntimeException) {
                throw (RuntimeException) exception.getCause().getCause();
            } else if (exception.getCause().getCause() instanceof Error) {
                throw (Error)exception.getCause().getCause();
            } else {
                throw exception;
            }
        }
    }

    private static class TestFramework<T> extends AbstractDummyArithmeticTest {
        private int numberOfParties;

        private TestFramework(int numberOfParties) {
            this.numberOfParties = numberOfParties;
        }

        private T run(Application<T, ProtocolBuilderNumeric> application) {
            Reference<T> output = new Reference<>();
            runTest(new TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric>() {
                @Override
                public TestThreadRunner.TestThread<DummyArithmeticResourcePool, ProtocolBuilderNumeric> next() {
                    return new TestThreadRunner.TestThread<DummyArithmeticResourcePool, ProtocolBuilderNumeric>() {
                        @Override
                        public void test() {
                            output.value = runApplication(application);
                        }
                    };
                }
            }, SEQUENTIAL, numberOfParties);
            return output.value;
        }
    }

    private static class Reference<T> {
        T value;
    }
}

