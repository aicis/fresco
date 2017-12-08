package dk.alexandra.fresco.tools.mascot.maccheck;

import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.FailedException;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.tools.commitment.Commitment;
import dk.alexandra.fresco.tools.commitment.CommitmentSerializer;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class MacCheck extends MultiPartyProtocol {

  protected CommitmentSerializer commSerializer;

  public MacCheck(MascotContext ctx) {
    super(ctx);
    this.commSerializer = new CommitmentSerializer();
  }

  /**
   * Sends own commitment to others and receives others' commitments.
   *
   * @param comm own commitment
   */
  List<Commitment> distributeCommitments(Commitment comm)
      throws IOException, ClassNotFoundException {
    // send own commitment
    network.sendToAll(ByteArrayHelper.serialize(comm));
    // receive other parties' commitments
    List<byte[]> rawComms = network.receiveFromAll();
    // parse
    List<Commitment> comms = rawComms.stream()
        .map(raw -> commSerializer.deserialize(raw))
        .collect(Collectors.toList());
    return comms;
  }

  /**
   * Sends own opening info to others and receives others' opening info.
   *
   * @param opening own opening info
   */
  List<Serializable> distributeOpenings(Serializable opening)
      throws IOException, ClassNotFoundException {
    // send own opening info
    network.sendToAll(ByteArrayHelper.serialize(opening));
    // receive opening info from others
    List<byte[]> rawOpenings = network.receiveFromAll();
    // parse
    List<Serializable> openings = rawOpenings.stream()
        .map(raw -> ByteArrayHelper.deserialize(raw))
        .collect(Collectors.toList());
    return openings;
  }

  /**
   * Attempts to open commitments using opening info, will throw if opening fails.
   * 
   * @param comms
   * @param openings
   * @return
   * @throws FailedCommitmentException
   * @throws MaliciousCommitmentException
   */
  List<FieldElement> open(List<Commitment> comms, List<Serializable> openings) {
    if (comms.size() != openings.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    List<FieldElement> result = new ArrayList<>(comms.size());
    for (int i = 0; i < comms.size(); i++) {
      Commitment comm = comms.get(i);
      Serializable opening = openings.get(i);
      FieldElement fe;
      try {
        fe = (FieldElement) comm.open(opening);
        result.add(fe);
      } catch (MaliciousCommitmentException e) {
        // TODO
        throw new MaliciousException(e);
      } catch (FailedCommitmentException e) {
        // TODO
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  /**
   * Runs mac-check on open value. <br>
   * Conceptually, computes that (macShare0 + ... + macShareN) = (open) * (keyShare0 + ... +
   * keyShareN)
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
      throw new FailedException(e);
    }

    List<Commitment> comms;
    List<Serializable> openings;
    try {
      // all parties commit
      comms = distributeCommitments(ownComm);
      // all parties send opening info
      openings = distributeOpenings(ownOpening);
    } catch (ClassNotFoundException | IOException e) {
      throw new FailedException("Serialization problem", e);
    }

    // open commitments using received opening info
    List<FieldElement> sigmas;
    sigmas = open(comms, openings);

    // add up all sigmas
    FieldElement sigmaSum = CollectionUtils.sum(sigmas);

    // sum of sigmas must be 0
    FieldElement zero = new FieldElement(0, modulus, modBitLength);
    if (!zero.equals(sigmaSum)) {
      throw new MaliciousException("Malicious mac forging detected");
    }
  }
}
