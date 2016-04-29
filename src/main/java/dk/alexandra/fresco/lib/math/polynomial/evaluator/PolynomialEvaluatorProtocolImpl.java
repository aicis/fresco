package dk.alexandra.fresco.lib.math.polynomial.evaluator;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.polynomial.Polynomial;

public class PolynomialEvaluatorProtocolImpl extends AbstractSimpleProtocol implements PolynomialEvaluatorProtocol {

	private final SInt x;
	private final Polynomial p;
	private SInt result;
	private final BasicNumericFactory basicNumericFactory;

	public PolynomialEvaluatorProtocolImpl(SInt x, Polynomial p, SInt result, BasicNumericFactory basicNumericFactory) {
		this.x = x;
		this.p = p;
		this.result = result;
		this.basicNumericFactory = basicNumericFactory;
	}
	
	@Override
	protected ProtocolProducer initializeGateProducer() {
		
		NumericProtocolBuilder builder = new NumericProtocolBuilder(basicNumericFactory);
		
		int degree = p.getMaxDegree();
		
		/*
		 * We use Horner's method, p(x) = (( ... ((p_{n-1} x + p_{n-2})x + p_{n-3}) ... )x + a_1)x + a_0
		 */
		SInt tmp = p.getCoefficient(degree - 1);
		builder.beginSeqScope();
		for (int i = degree - 2; i >= 0; i--) {
			tmp = builder.add(builder.mult(tmp, x), p.getCoefficient(i));
		}
		builder.endCurScope();
		
		return new SequentialProtocolProducer(builder.getCircuit(), new CopyProtocolImpl<SInt>(tmp, result));
	}

}
