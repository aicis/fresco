package dk.alexandra.fresco.lib.statistics;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AlgebraUtil;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPPrefix;

public abstract class DEAPrefixBuilder {

	protected List<SInt[]> basisInputs, basisOutputs;
	protected List<SInt> targetInputs, targetOutputs;
	protected BasicNumericFactory provider;
	protected ProtocolProducer prefix;

	protected final Logger log = Logger.getLogger("DEAPREFIX");
	
	/**
	 * Builds an LPPrefix from the given SInts, provider and prefix. Attempts to
	 * check if the values given are consistent before building the prefix. If
	 * this is not the case a IllegalStateException will be thrown.
	 * 
	 * @return an LPPrefix
	 */
	public abstract LPPrefix build();
	
	/**
	 * Returns a list of SInt arrays representing the basis inputs, i.e., those
	 * with which the target is compared against. One for each input type.
	 * 
	 * @return a list of SInt arrays representing the basis inputs.
	 */
	public List<SInt[]> getBasisInputs() {
		return basisInputs;
	}

	/**
	 * Returns a list of SInt arrays representing the basis outputs, i.e., those
	 * with which the target is compared against. One for each output type.
	 * 
	 * @return a list of SInt arrays representing the basis output.
	 */
	public List<SInt[]> getBasisOutputs() {
		return basisOutputs;
	}

	/**
	 * Returns a list of SInts. One for each input type.
	 * 
	 * @return a list of SInts representing the target inputs.
	 */
	public List<SInt> getTargetInputs() {
		return targetInputs;
	}

	/**
	 * Returns a list of SInts. One for each output type.
	 * 
	 * @return a list of SInts representing the target output.
	 */
	public List<SInt> getTargetOutputs() {
		return targetOutputs;
	}

	/**
	 * Returns the provider of this builder
	 * 
	 * @return the provider
	 */
	public BasicNumericFactory getProvider() {
		return provider;
	}

	/**
	 * Returns the GateProducer used to populate the inputs and outputs of this
	 * builder.
	 * 
	 * @return the prefix of this builder
	 */
	public ProtocolProducer getCircuit() {
		return prefix;
	}
	
	/**
	 * Creates a new instance of the builder.
	 * @return
	 */
	public abstract DEAPrefixBuilder createNewInstance();
	
	/**
	 * Constructs an empty builder
	 */
	public DEAPrefixBuilder() {
		basisInputs = new ArrayList<SInt[]>();
		basisOutputs = new ArrayList<SInt[]>();
		targetInputs = new ArrayList<SInt>();
		targetOutputs = new ArrayList<SInt>();
		provider = null;
		prefix = null;
	}

	/**
	 * Sets a list of SInt arrays used as the basis inputs by this builder. One
	 * array for each input type. Note that the order of this list should match
	 * that of the list given for target inputs. Otherwise the left and right
	 * hand sides of the constraints build by this builder will not match.
	 * 
	 * @param basisInputs
	 * @return this builder modified to set the list of basis inputs
	 */
	public DEAPrefixBuilder basisInputs(List<SInt[]> basisInputs) {
		this.basisInputs = basisInputs;
		return this;
	}

	/**
	 * Adds a SInt array to the list of basis inputs by this builder. Note that
	 * the order of this list should match that of the list given for target
	 * inputs. Otherwise the left and right hand sides of the constraints build
	 * by this builder will not match.
	 * 
	 * @param inputs
	 * @return this builder modified to add the input to the list of basis
	 *         inputs
	 */
	public DEAPrefixBuilder addBasisInput(SInt[] inputs) {
		if (this.basisInputs == null) {
			this.basisInputs = new ArrayList<SInt[]>();
		}
		this.basisInputs.add(inputs);
		return this;
	}

	/**
	 * Sets a list of SInt arrays used as the basis outputs by this builder. One
	 * array for each output type. Note that the order of this list should match
	 * that of the list given for target outputs. Otherwise the left and right
	 * hand sides of the constraints build by this builder will not match. Note
	 * also that basis output values should be negated compared to the values in
	 * the database.
	 * 
	 * @param basisOutputs
	 * @return this builder modified to set the list of basis outputs
	 */
	public DEAPrefixBuilder basisOutputs(List<SInt[]> basisOutputs) {
		this.basisOutputs = basisOutputs;
		return this;
	}

	/**
	 * Adds a SInt array to the list of outputs of basis by this builder. Note
	 * that the order of this list should match that of the list given for
	 * target outputs. Otherwise the left and right hand sides of the
	 * constraints build by this builder will not match.
	 * 
	 * @param outputs
	 * @return this builder modified to add the output to the list of basis
	 *         outputs
	 */
	public DEAPrefixBuilder addBasisOutput(SInt[] outputs) {
		if (this.basisOutputs == null) {
			this.basisOutputs = new ArrayList<SInt[]>();
		}
		this.basisOutputs.add(outputs);
		return this;
	}

	/**
	 * Sets a list of SInts used as the target inputs by this builder. One array
	 * for each input type. Note that the order of this list should match that
	 * of the list given for basis inputs. Otherwise the left and right hand
	 * sides of the constraints build by this builder will not match.
	 * 
	 * @param targetInputs
	 * @return this builder modified to set the list of target inputs
	 */
	public DEAPrefixBuilder targetInputs(List<SInt> targetInputs) {
		this.targetInputs = targetInputs;
		return this;
	}

	/**
	 * Adds a SInt to the list of inputs of the target by this builder. Note
	 * that the order of this list should match that of the list given for basis
	 * inputs. Otherwise the left and right hand sides of the constraints build
	 * by this builder will not match.
	 * 
	 * @param targetInput
	 * @return this builder modified to add the input to the list of the target
	 *         inputs
	 */
	public DEAPrefixBuilder addTargetInput(SInt targetInput) {
		if (this.targetInputs == null) {
			this.targetInputs = new ArrayList<SInt>();
		}
		this.targetInputs.add(targetInput);
		return this;
	}

	/**
	 * Sets a list of SInts used as the outputs of the target by this builder.
	 * One array for each output type. Note that the order of this list should
	 * match that of the list given for basis outputs. Otherwise the left and
	 * right hand sides of the constraints build by this builder will not match.
	 * 
	 * @param targetOutputs
	 * @return this builder modified to set the list of target outputs
	 */
	public DEAPrefixBuilder targetOutputs(List<SInt> targetOutputs) {
		this.targetOutputs = targetOutputs;
		return this;
	}

	/**
	 * Adds a SInt to the list of outputs of the target by this builder. Note
	 * that the order of this list should match that of the list given for basis
	 * outputs. Otherwise the left and right hand sides of the constraints build
	 * by this builder will not match.
	 * 
	 * @param targetOutput
	 * @return this builder modified to add the output to the list of the target
	 *         outputs
	 */
	public DEAPrefixBuilder addTargetOutput(SInt targetOutput) {
		if (this.targetOutputs == null) {
			this.targetOutputs = new ArrayList<SInt>();
		}
		this.targetOutputs.add(targetOutput);
		return this;
	}

	/**
	 * Sets the provider to be used by this builder. Note this should be the
	 * provider that provided the SInts and the prefix of this builder.
	 * 
	 * @param provider
	 * @return this builder with the provider set
	 */
	public DEAPrefixBuilder provider(BasicNumericFactory provider) {
		this.provider = provider;
		return this;
	}

	/**
	 * Sets the prefix of this builder which should populate all the SInts used
	 * be this builder
	 * 
	 * @param prefix
	 * @return this builder with the prefix set
	 */
	public DEAPrefixBuilder prefix(ProtocolProducer prefix) {
		this.prefix = prefix;
		return this;
	}

	/**
	 * Adds to the prefix of this builder. The new prefix is will run the
	 * current prefix in parallel with the given additional prefix. The joined
	 * prefix should populate all the SInts used be this builder
	 * 
	 * @param prefix
	 * @return this builder with added prefix
	 */
	public DEAPrefixBuilder addPrefix(ProtocolProducer addPrefix) {
		if (this.prefix == null) {
			this.prefix = addPrefix;
		} else {
			ProtocolProducer gp = new ParallelProtocolProducer(this.prefix, addPrefix);
			this.prefix = gp;
		}
		return this;
	}

	/**
	 * Appends the inputs and outputs of an other builder to this builder. The
	 * prefix is changed to be one evaluating the prefix of this builder and the
	 * other builder in parallel. Note this builder and the other builder must
	 * use the same provider or an IllegalArgumentException will be thrown. Also
	 * note the method will modify this builder, but not the appended builder.
	 * 
	 * @param builder
	 *            a builder
	 * @return this builder with the other builder appended
	 */
	public DEAPrefixBuilder append(DEAPrefixBuilder builder) {
		if (this.provider != builder.getProvider()) {
			throw new IllegalArgumentException(
					"Cannot append builder. Builders are not using the same provider.");
		}

		if (this.basisInputs == null) {
			this.basisInputs = builder.getBasisInputs();
		} else {
			if (builder.getBasisInputs() != null) {
				this.basisInputs.addAll(builder.getBasisInputs());
			}
		}

		if (this.basisOutputs == null) {
			this.basisOutputs = builder.getBasisOutputs();
		} else {
			if (builder.getBasisOutputs() != null) {
				this.basisOutputs.addAll(builder.getBasisOutputs());
			}
		}

		if (this.targetInputs == null) {
			this.targetInputs = builder.getTargetInputs();
		} else {
			if (builder.getTargetInputs() != null) {
				this.targetInputs.addAll(builder.getTargetInputs());
			}
		}

		if (this.targetOutputs == null) {
			this.targetOutputs = builder.getTargetOutputs();
		} else {
			if (builder.getTargetOutputs() != null) {
				this.targetOutputs.addAll(builder.getTargetOutputs());
			}
		}

		if (this.prefix == null) {
			this.prefix = builder.prefix;
		} else {
			if (builder.prefix != null) {
				ParallelProtocolProducer par = new ParallelProtocolProducer(
						this.prefix, builder.getCircuit());
				this.prefix = par;
			}
		}
		return this;
	}

	/**
	 * Returns a copy of this prefix builder, i.e. the SInts in the prefix of
	 * the returned builder will be copied from those in the prefix of this
	 * builder.
	 * 
	 * The copying will be done by the circuit part of the prefix of the
	 * returned builder. This means that the circuit of the prefix given by this
	 * builder should be evaluated strictly before that of the returned builder
	 * copy.
	 * 
	 * @return a copy of this prefix builder
	 */
	public DEAPrefixBuilder copy() {
		List<SInt[]> copyBasisInputs = new ArrayList<SInt[]>(this
				.getBasisInputs().size());
		ParallelProtocolProducer copyProducer = new ParallelProtocolProducer();
		for (SInt[] inputs : this.getBasisInputs()) {
			SInt[] copyInputs = AlgebraUtil
					.sIntFill(new SInt[inputs.length], provider);
			for (int i = 0; i < inputs.length; i++) {
				copyProducer.append(new CopyProtocolImpl<SInt>(inputs[i],
						copyInputs[i]));
			}
			copyBasisInputs.add(copyInputs);
		}

		List<SInt[]> copyBasisOutputs = new ArrayList<SInt[]>(this
				.getBasisOutputs().size());
		for (SInt[] outputs : this.getBasisOutputs()) {
			SInt[] copyOutputs = AlgebraUtil.sIntFill(new SInt[outputs.length],
					provider);
			for (int i = 0; i < outputs.length; i++) {
				copyProducer.append(new CopyProtocolImpl<SInt>(outputs[i],
						copyOutputs[i]));
			}
			copyBasisOutputs.add(copyOutputs);
		}

		List<SInt> copyTargetInputs = new ArrayList<SInt>(this
				.getTargetInputs().size());
		for (SInt input : this.getTargetInputs()) {
			SInt copyInput = provider.getSInt();
			copyProducer.append(new CopyProtocolImpl<SInt>(input, copyInput));
			copyTargetInputs.add(copyInput);
		}

		List<SInt> copyTargetOutputs = new ArrayList<SInt>(this
				.getTargetOutputs().size());
		for (SInt output : this.getTargetOutputs()) {
			SInt copyOutput = provider.getSInt();
			copyProducer.append(new CopyProtocolImpl<SInt>(output, copyOutput));
			copyTargetOutputs.add(copyOutput);
		}

		DEAPrefixBuilder copyBuilder = createNewInstance();
		copyBuilder = copyBuilder.basisInputs(copyBasisInputs)
				.basisOutputs(copyBasisOutputs).targetInputs(copyTargetInputs)
				.targetOutputs(copyTargetOutputs)
				.prefix(new SequentialProtocolProducer()).provider(this.provider);

		if(this.prefix != null){
			this.prefix = new SequentialProtocolProducer(this.prefix, copyProducer);
		} else {
			this.prefix = new SequentialProtocolProducer(copyProducer);
		}
		return copyBuilder;
	}
	
  /**
   * Checks if this builder is ready to be build. I.e. if all needed values
   * have been set.
   * 
   * @return true if all needed values are set. false otherwise.
   */
  public boolean ready() {
    boolean ready = true;
    ready = (basisInputs != null); // &&
    // basisOutputs != null &&
    // targetInputs != null &&
    // targetOutputs != null &&
    // provider != null &&
    // prefix != null &&
    // !basisInputs.isEmpty() &&
    // !basisOutputs.isEmpty() &&
    // !targetInputs.isEmpty() &&
    // !targetOutputs.isEmpty());
    return ready;
  }
	
	protected boolean consistent() {
	  System.out.println("first: "+basisInputs.size() +" - "+targetInputs.size());
		if (basisInputs.size() != targetInputs.size()) {
			log.warning("BasisInputs size (" + basisInputs.size()
					+ ")does not equal targetInputs size ("
					+ targetInputs.size() + ")");
			return false;
		}
    System.out.println("second: "+basisOutputs.size() +" - "+targetOutputs.size());
		if (basisOutputs.size() != targetOutputs.size()) {;
			log.warning("BasisOutputs size (" + basisOutputs.size()
					+ ")does not equal targetOutputs size ("
					+ targetOutputs.size() + ")");
			return false;
		}
		System.out.println("third: "+basisInputs.get(0).length);
		int dbSize = basisInputs.get(0).length;
		for (SInt[] inputs : basisInputs) {
		  if (inputs.length != dbSize) {
		    log.warning("All basisInputs does not agree on the length of the SInt array");
		    return false;
		  }
			
		}
		System.out.println("fourth ");
		for (SInt[] outputs : basisOutputs) {
		  if (outputs.length != dbSize) {
		    log.warning("All basisOutputs does not agree on the length of the SInt array");
		    return false;
		  }
		}
		return true;
	}

	protected static SInt[][] getIdentity(int dimension, BasicNumericFactory provider) {
		SInt[][] identity = new SInt[dimension][dimension];
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				if (i == j) {
					identity[i][j] = provider.getSInt(1);
				} else {
					identity[i][j] = provider.getSInt(0);
				}
			}
		}
		return identity;
	}

}
