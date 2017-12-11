package dk.alexandra.fresco.tools.mascot.commit;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.SecureSerializer;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.tools.commitment.Commitment;
import dk.alexandra.fresco.tools.commitment.CommitmentSerializer;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.broadcast.BroadcastValidation;
import dk.alexandra.fresco.tools.mascot.broadcast.BroadcastingNetworkDecorator;

// TODO need better name
public class CommitmentBasedProtocol<T> extends MultiPartyProtocol {

  CommitmentSerializer commSerializer;
  SecureSerializer<T> serializer;
  Network broadcaster;

  public CommitmentBasedProtocol(MascotContext ctx, SecureSerializer<T> serializer) {
    super(ctx);
    this.commSerializer = new CommitmentSerializer();
    this.serializer = serializer;
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
  protected List<Commitment> distributeCommitments(Commitment comm) {
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
  protected List<Serializable> distributeOpenings(Serializable opening) {
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
  protected List<T> open(List<Commitment> comms, List<Serializable> openings) {
    if (comms.size() != openings.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    List<T> result = new ArrayList<>(comms.size());
    for (int i = 0; i < comms.size(); i++) {
      Commitment comm = comms.get(i);
      Serializable opening = openings.get(i);
      // T el = serializer.deserialize(comm.open(opening));
      // this will go away as soon as commitments are fixed
      T fe = (T) comm.open(opening);
      result.add(fe);
    }
    return result;
  }

  protected List<T> allCommit(T value) {
    // commit to sigma
    Commitment ownComm = new Commitment(modBitLength);

    // TODO this will go away once serialization is fixed
    Serializable ownOpening = ownComm.commit(new SecureRandom(), (Serializable) value);
    // commit to value locally
    // Serializable ownOpening = ownComm.commit(new SecureRandom(), serializer.serialize(value));

    // all parties commit
    List<Commitment> comms = distributeCommitments(ownComm);;

    // all parties send opening info
    List<Serializable> openings = distributeOpenings(ownOpening);

    // open commitments using received opening info
    return open(comms, openings);
  }

}
