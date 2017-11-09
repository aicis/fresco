package dk.alexandra.fresco.tools.mascot.maccheck;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;

public class DummyMacCheck extends MultiPartyProtocol implements MacCheck {

  public DummyMacCheck(Integer myId, List<Integer> partyIds, BigInteger modulus, int kBitLength,
      ExtendedNetwork network, ExecutorService executor, Random rand) {
    super(myId, partyIds, modulus, kBitLength, network, executor, rand);
  }

  @Override
  public void check(FieldElement opened, FieldElement macKeyShare, FieldElement macShare)
      throws MPCException {
    FieldElement sigma = macShare.subtract(opened.multiply(macKeyShare));
    List<FieldElement> sigmas = new ArrayList<>();
    for (Integer partyId : partyIds) {
      if (myId.equals(partyId)) {
        network.sendToAll(sigma.toByteArray());
      } else {
        try {
          BigInteger raw = new BigInteger(network.receive(0, partyId));
          sigmas.add(new FieldElement(raw, modulus, kBitLength));
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    sigmas.add(sigma);
    FieldElement zero = new FieldElement(0, modulus, kBitLength);
    FieldElement sigmaSum = sigmas.stream().reduce(zero, (left, right) -> left.add(right));
    if (!zero.equals(sigmaSum)) {
      throw new MPCException("Mac check failed!");
    }
  }

}
