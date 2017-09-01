package dk.alexandra.fresco.suite.tinytables.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import java.security.SecureRandom;

/**
 * This factory produces {@link BaseOTSender}'s and {@link BaseOTReceiver}'s, which are wrappers
 * around SCAPI's {@link OTSemiHonestDDHBatchOnByteArraySender} and
 * {@link OTSemiHonestDDHBatchOnByteArrayReceiver} resp.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class BaseOTFactory implements dk.alexandra.fresco.suite.tinytables.ot.OTFactory {

  private Network network;
  private int myId;
  private SecureRandom random;

  public BaseOTFactory(Network network, int myId, SecureRandom random) {
    this.network = network;
    this.myId = myId;
    this.random = random;
  }

  @Override
  public dk.alexandra.fresco.suite.tinytables.ot.OTSender createOTSender() {
    return new BaseOTSender(network, myId, random);
  }

  @Override
  public dk.alexandra.fresco.suite.tinytables.ot.OTReceiver createOTReceiver() {
    return new BaseOTReceiver(network, myId, random);
  }

}
