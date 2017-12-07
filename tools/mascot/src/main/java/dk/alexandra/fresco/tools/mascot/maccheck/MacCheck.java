package dk.alexandra.fresco.tools.mascot.maccheck;

import dk.alexandra.fresco.framework.FailedException;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.tools.commitment.Commitment;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class MacCheck extends MultiPartyProtocol {

  public MacCheck(MascotContext ctx) {
    super(ctx);
  }

  /**
   * Sends own commitment to others and receives others' commitments.
   *
   * @param comm own commitment
   */
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

  /**
   * Sends own opening info to others and receives others' opening info.
   *
   * @param opening own opening info
   */
  List<Serializable> distributeOpenings(Serializable opening)
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

  /**
   * Attempts to open commitments using opening info.
   */
  List<FieldElement> open(List<Commitment> comms, List<Serializable> openings)
      throws FailedCommitmentException, MaliciousCommitmentException {
    if (comms.size() != openings.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    List<FieldElement> result = new ArrayList<>(comms.size());
    for (int i = 0; i < comms.size(); i++) {
      Commitment comm = comms.get(i);
      Serializable opening = openings.get(i);
      FieldElement fe;
      fe = (FieldElement) comm.open(opening);
      result.add(fe);
    }
    return result;
  }

  /**
   * Runs mac-check on open value. <b> Conceptually, computes that (macShare0 + ... + macShareN) =
   * (open) * (keyShare0 + ... + keyShareN)
   */
  public void check(FieldElement opened, FieldElement macKeyShare, FieldElement macShare) {
    // we will check that all sigmas together add up to 0
    FieldElement sigma = macShare.subtract(opened.multiply(macKeyShare));

    // commit to sigma
    Commitment ownComm = new Commitment(modBitLength);

    Serializable ownOpening;
    try {
      ownOpening = ownComm.commit(new SecureRandom(), sigma);
    } catch (FailedCommitmentException e) {
      throw new FailedException("Non-malicious failure during initial commit", e);
    }

    List<Commitment> comms;
    List<Serializable> openings;
    try {
      // all parties commit
      comms = distributeCommitments(ownComm);
      // all parties send opening info
      openings = distributeOpenings(ownOpening);
    } catch (ClassNotFoundException | IOException e) {
      throw new FailedException("Non-malicious failure during distribution phase", e);
    }

    // open commitments using received opening info
    List<FieldElement> sigmas;
    try {
      sigmas = open(comms, openings);
    } catch (FailedCommitmentException e) {
      throw new FailedException("redundant rethrow - wil be removed", e);
    } catch (MaliciousCommitmentException e) {
      throw new MaliciousException("redundant rethrow - wil be removed", e);
    }

    // add up all sigmas
    FieldElement sigmaSum = CollectionUtils.sum(sigmas);

    // sum of sigmas must be 0
    FieldElement zero = new FieldElement(0, modulus, modBitLength);
    if (!zero.equals(sigmaSum)) {
      throw new MaliciousException("Malicious mac forging detected");
    }

  }

}
