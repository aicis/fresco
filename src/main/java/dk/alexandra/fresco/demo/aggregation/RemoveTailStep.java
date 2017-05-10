package dk.alexandra.fresco.demo.aggregation;

import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.sce.SCE;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.SortingProtocolBuilder;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.Arrays;

public class RemoveTailStep implements Application {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OInt[][] result;
    private SInt[][] resultWithTail;
    private int tailLength;

    public RemoveTailStep(SInt[][] resultWithTail, int tailLength) {
    	this.resultWithTail = resultWithTail;
    	this.tailLength = tailLength;
//    	System.out.println(this.resultWithTail);
    }
    
    public OInt[][] getResult() {
        return this.result;
    }
    
    @Override
    public ProtocolProducer prepareApplication(ProtocolFactory factory) {
        
        OmniBuilder builder = new OmniBuilder(factory);

        NumericIOBuilder niob = builder.getNumericIOBuilder();                
        
        builder.beginSeqScope();

        	SInt[][] withoutTail = Arrays.copyOfRange(
        			resultWithTail, tailLength, resultWithTail.length);
            result = niob.outputMatrix(withoutTail);

        builder.endCurScope();
            
        return builder.getProtocol();
    }
}
