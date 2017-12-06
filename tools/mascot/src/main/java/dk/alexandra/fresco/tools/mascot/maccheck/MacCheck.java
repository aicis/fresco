package dk.alexandra.fresco.tools.mascot.maccheck;

import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.tools.commitment.Commitment;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class MacCheck extends MultiPartyProtocol {

  public MacCheck(MascotContext ctx) {
    super(ctx);
  }

  /**
   * Sends own commitment to others and receives others' commitments.
   * 
   * @param comm own commitment
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
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
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
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
   * 
   * @param comms
   * @param openings
   * @return
   * @throws MaliciousMacCheckException
   * @throws FailedMacCheckException
   */
  List<FieldElement> open(List<Commitment> comms, List<Serializable> openings)
      throws MaliciousMacCheckException, FailedMacCheckException {
    if (comms.size() != openings.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    List<FieldElement> result = new ArrayList<>(comms.size());
    try {
      for (int i = 0; i < comms.size(); i++) {
        Commitment comm = comms.get(i);
        Serializable opening = openings.get(i);
        FieldElement fe;
        fe = (FieldElement) comm.open(opening);
        result.add(fe);
      }
    } catch (MaliciousCommitmentException e) {
      throw new MaliciousMacCheckException("Malicious exception while opening commitments", e);
    } catch (FailedCommitmentException e) {
      throw new FailedMacCheckException("Non-malicious exception while opening commitments", e);
    }
    return result;
  }

  /**
   * Runs mac-check on open value. <b> Conceptually, computes that (macShare0 + ... + macShareN) =
   * (open) * (keyShare0 + ... + keyShareN)
   * 
   * @param opened
   * @param macKeyShare
   * @param macShare
   * @throws MaliciousMacCheckException
   * @throws FailedMacCheckException
   */
  public void check(FieldElement opened, FieldElement macKeyShare, FieldElement macShare)
      throws MaliciousMacCheckException, FailedMacCheckException {
    // we will check that all sigmas together add up to 0
    FieldElement sigma = macShare.subtract(opened.multiply(macKeyShare));

    // commit to sigma
    Commitment ownComm = new Commitment(modBitLength);

    Serializable ownOpening;
    try {
      ownOpening = ownComm.commit(new SecureRandom(), sigma);
    } catch (FailedCommitmentException e) {
      throw new FailedMacCheckException("Non-malicious failure during initial commit", e);
    }

    List<Commitment> comms = null;
    List<Serializable> openings = null;
    try {
      // all parties commit
      comms = distributeCommitments(ownComm);
      // all parties send opening info
      openings = distributeOpenings(ownOpening);
    } catch (ClassNotFoundException | IOException e) {
      throw new FailedMacCheckException("Non-malicious failure during distribution phase", e);
    }

    // open commitments using received opening info
    List<FieldElement> sigmas = open(comms, openings);

    // add up all sigmas
    FieldElement sigmaSum = CollectionUtils.sum(sigmas);

    // sum of sigmas must be 0
    FieldElement zero = new FieldElement(0, modulus, modBitLength);
    if (!zero.equals(sigmaSum)) {
      throw new MaliciousMacCheckException("Malicious mac forging detected");
    }

  }

}
