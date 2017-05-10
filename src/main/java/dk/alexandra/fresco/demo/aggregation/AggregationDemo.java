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

public class AggregationDemo {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
    public void runApplication(SCE sce) {
    	
    	int[][] inputTuples = new int[][]{
            {1, 10},
            {1, 7},
            {2, 100},
            {1, 50},
            {3, 15},
            {2, 70}
        };
        
        AggregationStep aggStep = new AggregationStep(inputTuples);
        sce.runApplication(aggStep);
        RemoveTailStep remTailStep = new RemoveTailStep(
        	aggStep.getResultWithTail(), 
        	aggStep.getEmptyTuplesCount().getValue().intValue()
        );
        sce.runApplication(remTailStep);
        OInt[][] result = remTailStep.getResult();
        
        for (OInt[] tuple: result) {
        	System.out.println(
        		tuple[0].getValue() + " " + tuple[1].getValue()
        	);
        }
    }
    
    public static void main(String[] args) {

        // My player ID
        int myPID = Integer.parseInt(args[0]);

        // Set up our SCE configuration
        SCEConfiguration sceConfig = new SCEConfiguration() {

            @Override
            public int getMyId() {
                return myPID;
            }
            
            @Override
            public String getProtocolSuiteName() {
                // We will use the SPDZ backend suite.
                return "spdz";
            }

            @Override
            public Map<Integer, Party> getParties() {
                // Set up network details of our two players
                Map<Integer,Party> parties = new HashMap<Integer,Party>();
                parties.put(1, new Party(1, "localhost", 8001));
                parties.put(2, new Party(2, "localhost", 8002));
                return parties;
            }

            @Override
            public Level getLogLevel() {
                return Level.INFO;
            }

            @Override
            public int getNoOfThreads() {
                return 2;
            }
            
            @Override
            public int getNoOfVMThreads() {
                return 2;
            }
            
            @Override
            public ProtocolEvaluator getEvaluator() {
                // We will use a sequential evaluation strategy
                ProtocolEvaluator evaluator = new SequentialEvaluator();
                return evaluator;
            }

            @Override
            public Storage getStorage() {
                return new InMemoryStorage();
            }

            @Override
            public int getMaxBatchSize() {
                return 4096;
            }

            @Override
            public StreamedStorage getStreamedStorage() {
                // We will not use StreamedStorage
                return null;
            }

            @Override
            public NetworkingStrategy getNetwork() {
                return NetworkingStrategy.KRYONET;
            }
        };

        ProtocolSuiteConfiguration protocolSuiteConfig = new SpdzConfiguration() {
            @Override
            public PreprocessingStrategy getPreprocessingStrategy() {
                return PreprocessingStrategy.DUMMY;
            }
            @Override
            public String fuelStationBaseUrl() {
                return null;
            }
            @Override
            public int getMaxBitLength() {
                return 150;
            }
        };

        // Instantiate environment
        SCE sce = SCEFactory.getSCEFromConfiguration(sceConfig, protocolSuiteConfig);
        
        // Create application we are going run
        AggregationDemo app = new AggregationDemo();
        
		app.runApplication(sce);
        
        return;

    }
}
