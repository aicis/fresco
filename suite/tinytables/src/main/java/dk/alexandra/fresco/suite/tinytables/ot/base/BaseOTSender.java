package dk.alexandra.fresco.suite.tinytables.ot.base;

import dk.alexandra.fresco.framework.network.CloseableNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.OTBatchOnByteArraySInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.semiHonest.OTSemiHonestDDHBatchOnByteArraySender;
import edu.biu.scapi.tools.Factories.KdfFactory;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * This OTSender is a wrapper around SCAPI's {@link OTSemiHonestDDHBatchOnByteArraySender} based on
 * an elliptic curve over a finite field, namely the K-163 curve.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class BaseOTSender implements dk.alexandra.fresco.suite.tinytables.ot.OTSender {

  private Network network;
  private int myId;
  private SecureRandom random;

  /*
   * We keep a singleton of the actual sender.
   */
  private static OTSemiHonestDDHBatchOnByteArraySender sender;

  private OTSemiHonestDDHBatchOnByteArraySender getInstance(SecureRandom random) {
    if (sender == null) {
      sender = ExceptionConverter.safe(
          () -> new OTSemiHonestDDHBatchOnByteArraySender(
              new edu.biu.scapi.primitives.dlog.bc.BcDlogECF2m(),
              // new edu.biu.scapi.primitives.dlog.openSSL.OpenSSLDlogECF2m(),
              KdfFactory.getInstance().getObject("HKDF(HMac(SHA-256))"), random),
          "Unable to construct OT sender");
    }

    return sender;
  }

  public BaseOTSender(Network network, int myId, SecureRandom random) {
    this.network = network;
    this.myId = myId;
    this.random = random;
  }

  @Override
  public void send(List<dk.alexandra.fresco.suite.tinytables.ot.datatypes.OTInput> inputs) {

    ArrayList<byte[]> x0 = new ArrayList<>();
    ArrayList<byte[]> x1 = new ArrayList<>();
    for (dk.alexandra.fresco.suite.tinytables.ot.datatypes.OTInput input : inputs) {
      x0.add(dk.alexandra.fresco.suite.tinytables.ot.Encoding.encodeBitSet(input.getX0(),
          input.getLength()));
      x1.add(dk.alexandra.fresco.suite.tinytables.ot.Encoding.encodeBitSet(input.getX1(),
          input.getLength()));
    }

    OTBatchOnByteArraySInput otsInputs = new OTBatchOnByteArraySInput(x0, x1);
    OTSemiHonestDDHBatchOnByteArraySender sender = getInstance(random);

    ExceptionConverter.safe(
        () -> sender.transfer(new NetworkWrapper((CloseableNetwork) network, myId), otsInputs),
        "Unable to transer OT inputs");
  }

}
