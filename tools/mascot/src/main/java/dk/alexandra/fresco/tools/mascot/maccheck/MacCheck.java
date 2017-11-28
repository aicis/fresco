package dk.alexandra.fresco.tools.mascot.maccheck;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class MacCheck extends BaseProtocol {

  public MacCheck(MascotContext ctx) {
    super(ctx);
  }

  public void check(FieldElement opened, FieldElement macKeyShare, FieldElement macShare)
      throws MPCException {
    List<Integer> partyIds = ctx.getPartyIds();
    Integer myId = ctx.getMyId();
    Network network = ctx.getNetwork();
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();

    FieldElement sigma = macShare.subtract(opened.multiply(macKeyShare));
    
    // TODO: use commitment 
    List<FieldElement> sigmas = new ArrayList<>();
    for (Integer partyId : partyIds) {
      if (myId.equals(partyId)) {
        network.sendToAll(sigma.toByteArray());
      } else {
        sigmas.add(new FieldElement(network.receive(partyId), modulus, modBitLength));
      }
    }
    sigmas.add(sigma);
    FieldElement zero = new FieldElement(0, modulus, modBitLength);
    FieldElement sigmaSum = sigmas.stream()
        .reduce(zero, (left, right) -> left.add(right));
    if (!zero.equals(sigmaSum)) {
      throw new MPCException("Mac check failed!");
    }
  }

}
