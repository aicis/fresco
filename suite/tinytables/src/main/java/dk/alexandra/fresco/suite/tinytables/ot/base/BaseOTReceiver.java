package dk.alexandra.fresco.suite.tinytables.ot.base;

import dk.alexandra.fresco.framework.network.CloseableNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.suite.tinytables.ot.datatypes.OTSigma;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.OTBatchOnByteArrayROutput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.OTBatchRBasicInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.semiHonest.OTSemiHonestDDHBatchOnByteArrayReceiver;
import edu.biu.scapi.tools.Factories.KdfFactory;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * This OTReciever is a wrapper around SCAPI's {@link OTSemiHonestDDHBatchOnByteArrayReceiver} based
 * on an elliptic curve over a finite field, namely the K-163 curve.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class BaseOTReceiver implements dk.alexandra.fresco.suite.tinytables.ot.OTReceiver {

  private Network network;
  private int myId;
  private SecureRandom random;

  /*
   * We keep a singleton of the actual receiver.
   */
  private static OTSemiHonestDDHBatchOnByteArrayReceiver receiver;

  private OTSemiHonestDDHBatchOnByteArrayReceiver getInstance(SecureRandom random) {
    if (receiver == null) {
      receiver = ExceptionConverter.safe(
          () -> new OTSemiHonestDDHBatchOnByteArrayReceiver(
              new edu.biu.scapi.primitives.dlog.bc.BcDlogECF2m(),
              // new edu.biu.scapi.primitives.dlog.openSSL.OpenSSLDlogECF2m(),
              KdfFactory.getInstance().getObject("HKDF(HMac(SHA-256))"), random),
          "Unable to construct OTReceiver");
    }
    return receiver;
  }

  public BaseOTReceiver(Network network, int myId, SecureRandom random) {
    this.network = network;
    this.myId = myId;
    this.random = random;
  }

  @Override
  public List<BitSet> receive(List<OTSigma> sigmas, int expectedLength) {

    OTSemiHonestDDHBatchOnByteArrayReceiver receiver = getInstance(random);

    ArrayList<Byte> s = new ArrayList<>();
    for (OTSigma sigma : sigmas) {
      s.add(dk.alexandra.fresco.suite.tinytables.ot.Encoding.encodeBoolean(sigma.getSigma()));
    }
    OTBatchRBasicInput input = new OTBatchRBasicInput(s);
    OTBatchOnByteArrayROutput output = ExceptionConverter.safe(() ->
    (OTBatchOnByteArrayROutput) receiver
        .transfer(new NetworkWrapper((CloseableNetwork)network, myId), input),
        "Unable to transfer OT input");

    List<BitSet> results = new ArrayList<>();
    for (int i = 0; i < sigmas.size(); i++) {
      results.add(dk.alexandra.fresco.suite.tinytables.ot.Encoding
          .decodeBitSet(output.getXSigmaArr().get(i)));
    }

    return results;
  }

}
