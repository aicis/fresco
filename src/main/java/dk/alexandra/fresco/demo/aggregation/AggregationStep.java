package dk.alexandra.fresco.demo.aggregation;

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

public class AggregationStep implements Application {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private int[][] inputTuples; 
    private OInt[][] result;
    private SInt[][] resultWithTail;
    private OInt emptyTuplesCount;

    AggregationStep(int[][] inputTuples) {
        this.inputTuples = inputTuples;
    }

    public OInt[][] getResult() {
        return this.result;
    }
    
    public SInt[][] getResultWithTail() {
    	return this.resultWithTail;
    }
    
    public OInt getEmptyTuplesCount() {
    	return this.emptyTuplesCount;
    }
    
    @Override
    public ProtocolProducer prepareApplication(ProtocolFactory factory) {
        
        OmniBuilder builder = new OmniBuilder(factory);

        // Get specific protocol builders to make code more concise
        NumericProtocolBuilder npb = builder.getNumericProtocolBuilder();
        NumericIOBuilder niob = builder.getNumericIOBuilder();                
        SortingProtocolBuilder spb = builder.getSortingProtocolBuilder();
        
        int keyIndex = 0;
        int valueIndex = 1;
        
        builder.beginSeqScope();

        	// Player 1 provides all input            
        	SInt[][] tuples = niob.inputMatrix(this.inputTuples, 1);

        	// Sort by key, with side-effect on tuples        	
            spb.keyedSort(tuples, keyIndex);
            
            SInt equalNeighborsCounter = npb.getSInt(0);
            // Aggregation            
            for (int rightIndex = 1; rightIndex < tuples.length; rightIndex++) {
				
            	int leftIndex = rightIndex - 1;
				
            	SInt[] leftTuple = tuples[leftIndex];
            	SInt[] rightTuple = tuples[rightIndex];
            	
            	// Compare keys				
            	SInt areKeysEqual = spb.compareEqual(
            			leftTuple[keyIndex], rightTuple[keyIndex]);
            	equalNeighborsCounter = npb.add(equalNeighborsCounter, areKeysEqual);
            	SInt areKeysNotEqual = npb.sub(npb.knownOInt(1), areKeysEqual);
                
            	// new left value is 0 if keys are not equal, unchanged otherwise            	
				SInt updatedLeftValue = npb.mult(leftTuple[valueIndex], areKeysNotEqual);
				
				// new right value is original value plus left value if keys are equal,
				// unchanged otherwise				
				SInt updatedRightValue = npb.mult(leftTuple[valueIndex], areKeysEqual);
				updatedRightValue = npb.add(rightTuple[valueIndex], updatedRightValue);
						
				// Update the values in our collection of tuples				
	            tuples[leftIndex][valueIndex] = updatedLeftValue;
	            tuples[rightIndex][valueIndex] = updatedRightValue;
	            
			}
            
            // Sort by values to push all empty entries to the tail
            // with side-effect on tuples        	
            spb.keyedSort(tuples, valueIndex);
            
            emptyTuplesCount = niob.output(equalNeighborsCounter);
            resultWithTail = tuples;

        builder.endCurScope();
            
        return builder.getProtocol();
    }
}
