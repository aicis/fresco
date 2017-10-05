package dk.alexandra.fresco.framework.sce.configuration;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class TestSCEConfiguration<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> {

  private NetworkingStrategy networkingStrategy;
  private ProtocolEvaluator<ResourcePoolT, Builder> evaluator;
  private final ProtocolSuite<ResourcePoolT, Builder> suite;
  private NetworkConfiguration networkConfiguration;

  public TestSCEConfiguration(ProtocolSuite<ResourcePoolT, Builder> suite,
      NetworkingStrategy networkingStrategy, ProtocolEvaluator<ResourcePoolT, Builder> evaluator,
      NetworkConfiguration conf, boolean useSecureConn) {
    this.suite = suite;
    this.networkingStrategy = networkingStrategy;
    this.evaluator = evaluator;
    evaluator.setMaxBatchSize(4096);
    networkConfiguration = conf;
    for (int i = 1; i <= conf.noOfParties(); i++) {
      if (useSecureConn) {
        Party p = conf.getParty(i);
        // Use the same hardcoded test 128 bit AES key for all connections
        p.setSecretSharedKey("w+1qn2ooNMCN7am9YmYQFQ==");
      }
    }
  }

  public ProtocolSuite<ResourcePoolT, Builder> getSuite() {
    return suite;
  }

  public NetworkConfiguration getNetworkConfiguration() {
    return networkConfiguration;
  }

  public ProtocolEvaluator<ResourcePoolT, Builder> getEvaluator() {
    return this.evaluator;
  }

  public NetworkingStrategy getNetworkStrategy() {
    return this.networkingStrategy;
  }

}
