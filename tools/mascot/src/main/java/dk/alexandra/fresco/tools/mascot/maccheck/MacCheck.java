package dk.alexandra.fresco.tools.mascot.maccheck;

import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.tools.commitment.Commitment;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class MacCheck extends MultiPartyProtocol {

  public MacCheck(MascotContext ctx) {
    super(ctx);
  }

  List<Commitment> distributeCommitments(Commitment comm)
      throws IOException, ClassNotFoundException {
    // all commitments
    List<Commitment> comms = new ArrayList<>();
    comms.add(comm);
    // send own commitment
    network.sendToAll(ByteArrayHelper.serialize(comm));
    // receive other parties' commitments
    for (Integer partyId : partyIds) {
      if (!myId.equals(partyId)) {
        Commitment otherComm = Commitment.receiveCommitment(partyId, network);
        comms.add(otherComm);
      }
    }
    return comms;
  }

  List<Serializable> distributeOpenings(Serializable opening, List<Commitment> comms)
      throws IOException, ClassNotFoundException {
    // all openings
    List<Serializable> openings = new ArrayList<>();
    openings.add(opening);
    // send own opening info
    network.sendToAll(ByteArrayHelper.serialize(opening));
    // receive opening info from others
    for (Integer partyId : partyIds) {
      if (!myId.equals(partyId)) {
        Serializable otherOpening = ByteArrayHelper.deserialize(network.receive(partyId));
        openings.add(otherOpening);
      }
    }
    return openings;
  }

  List<FieldElement> open(List<Commitment> comms, List<Serializable> openings) {
    return IntStream.range(0, comms.size())
        .mapToObj(idx -> {
          Commitment comm = comms.get(idx);
          Serializable opening = openings.get(idx);
          try {
            return (FieldElement) comm.open(opening);
          } catch (MaliciousCommitmentException | FailedCommitmentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
          }
        })
        .collect(Collectors.toList());
  }

  public void check(FieldElement opened, FieldElement macKeyShare, FieldElement macShare)
      throws MPCException, IOException, ClassNotFoundException, FailedCommitmentException {
    // we will check that all sigmas together add up to 0
    FieldElement sigma = macShare.subtract(opened.multiply(macKeyShare));

    // commit to sigma
    Commitment ownComm = new Commitment(modBitLength);
    Serializable ownOpening = ownComm.commit(new SecureRandom(), sigma);

    // all parties commit
    List<Commitment> comms = distributeCommitments(ownComm);

    // all parties send opening info
    List<Serializable> openings = distributeOpenings(ownOpening, comms);

    // open commitments using received opening info
    List<FieldElement> sigmas = open(comms, openings);

    // add up all sigmas
    FieldElement sigmaSum = sigmas.stream()
        .reduce((left, right) -> left.add(right))
        .get();

    // sum of sigmas must be 0
    FieldElement zero = new FieldElement(0, modulus, modBitLength);
    if (!zero.equals(sigmaSum)) {
      throw new MPCException("Mac check failed!");
    }
  }

}
