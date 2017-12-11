package dk.alexandra.fresco.tools.mascot.maccheck;

import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.tools.commitment.Commitment;
import dk.alexandra.fresco.tools.commitment.CommitmentSerializer;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
import dk.alexandra.fresco.tools.mascot.broadcast.BroadcastValidation;
import dk.alexandra.fresco.tools.mascot.broadcast.BroadcastingNetworkDecorator;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class MacCheck extends MultiPartyProtocol {

  CommitmentSerializer commSerializer;
  Network broadcaster;

  /**
   * Constructs new mac checker.
   * 
   * @param ctx
   */
  public MacCheck(MascotContext ctx) {
    super(ctx);
    this.commSerializer = new CommitmentSerializer();
    // for more than two parties, we need to use broadcast
    if (partyIds.size() > 2) {
      this.broadcaster = new BroadcastingNetworkDecorator(network, new BroadcastValidation(ctx));
    } else {
      // if we have two parties or less we can just use the regular network
      this.broadcaster = this.network;
    }
  }

  /**
   * Sends own commitment to others and receives others' commitments.
   *
   * @param comm own commitment
   */
  List<Commitment> distributeCommitments(Commitment comm)
      throws IOException, ClassNotFoundException {
    // broadcast own commitment
    broadcaster.sendToAll(ByteArrayHelper.serialize(comm));
    // receive other parties' commitments from broadcast
    List<byte[]> rawComms = broadcaster.receiveFromAll();
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
    // send (over regular network) own opening info
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
      fe = (FieldElement) comm.open(opening);
      result.add(fe);
    }
    return result;
  }

  /**
   * Runs mac-check on open value. <br>
   * Conceptually, computes that (macShare0 + ... + macShareN) = (open) * (keyShare0 + ... +
   * keyShareN)
   * 
   * @param opened the opened element to validate
   * @param macKeyShare this party's share of the mac key
   * @param macShare this party's share of the mac
   * @return
   * @throws MaliciousException if mac-check fails
   */
  public void check(FieldElement opened, FieldElement macKeyShare, FieldElement macShare) {
    // we will check that all sigmas together add up to 0
    FieldElement sigma = macShare.subtract(opened.multiply(macKeyShare));

    // commit to sigma
    Commitment ownComm = new Commitment();

    Serializable ownOpening;
    ownOpening = ownComm.commit(new SecureRandom(), sigma);

    List<Commitment> comms;
    List<Serializable> openings;
    try {
      // all parties commit
      comms = distributeCommitments(ownComm);
      // all parties send opening info
      openings = distributeOpenings(ownOpening);
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException("Serialization problem", e);
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
