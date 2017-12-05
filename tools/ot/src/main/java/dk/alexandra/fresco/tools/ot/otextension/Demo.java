package dk.alexandra.fresco.tools.ot.otextension;

import java.util.HashMap;
import java.util.Map;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;

public class Demo {
  // Computational security parameter
  private int kbitLength = 128;
  // Statistical security parameter
  private int lambdaSecurityParam = 40;

  /**
   * Return a default networkConfiguration
   * 
   * @param pid
   *          ID of the calling party
   * @return Map containing default network configuration.
   */
  public static NetworkConfiguration getNetworkConfiguration(int pid) {
    Map<Integer, Party> parties = new HashMap<>();
    parties.put(1, new Party(1, "localhost", 8001));
    parties.put(2, new Party(2, "localhost", 8002));
    return new NetworkConfigurationImpl(pid, parties);
  }

  public int getKbitLength() {
    return kbitLength;
  }

  public int getLambdaSecurityParam() {
    return lambdaSecurityParam;
  }
}
