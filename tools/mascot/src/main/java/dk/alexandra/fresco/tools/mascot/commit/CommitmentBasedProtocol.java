package dk.alexandra.fresco.tools.mascot.commit;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.SecureSerializer;
import dk.alexandra.fresco.tools.commitment.Commitment;
import dk.alexandra.fresco.tools.commitment.CommitmentSerializer;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.broadcast.BroadcastValidation;
import dk.alexandra.fresco.tools.mascot.broadcast.BroadcastingNetworkDecorator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// TODO need better name
public class CommitmentBasedProtocol<T> extends MultiPartyProtocol {

  SecureSerializer<T> serializer;
  Network broadcaster;

  /**
   * Creates new {@link CommitmentBasedProtocol}.
   */
  public CommitmentBasedProtocol(MascotResourcePool resourcePool, Network network,
      SecureSerializer<T> serializer) {
    super(resourcePool, network);
    this.serializer = serializer;
    // for more than two parties, we need to use broadcast
    if (resourcePool.getNoOfParties() > 2) {
      this.broadcaster =
          new BroadcastingNetworkDecorator(network, new BroadcastValidation(resourcePool, network));
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
    broadcaster.sendToAll(getCommitmentSerializer().serialize(comm));
    // receive other parties' commitments from broadcast
    List<byte[]> rawComms = broadcaster.receiveFromAll();
    // parse
    List<Commitment> comms = rawComms.stream()
        .map(raw -> getCommitmentSerializer().deserialize(raw))
        .collect(Collectors.toList());
    return comms;
  }

  /**
   * Sends own opening info to others and receives others' opening info.
   *
   * @param opening own opening info
   */
  protected List<byte[]> distributeOpenings(byte[] opening) {
    // send (over regular network) own opening info
    network.sendToAll(opening);
    // receive opening info from others
    List<byte[]> openings = network.receiveFromAll();
    return openings;
  }

  /**
   * Attempts to open commitments using opening info, will throw if opening fails.
   * 
   * @param comms commitments
   * @param openings opening information
   * @return values from opened commitments
   */
  protected List<T> open(List<Commitment> comms, List<byte[]> openings) {
    if (comms.size() != openings.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    List<T> result = new ArrayList<>(comms.size());
    for (int i = 0; i < comms.size(); i++) {
      Commitment comm = comms.get(i);
      byte[] opening = openings.get(i);
      T el = serializer.deserialize(comm.open(opening));
      result.add(el);
    }
    return result;
  }

  protected List<T> allCommit(T value) {
    // commit to sigma
    Commitment ownComm = new Commitment();

    // commit to value locally
    byte[] ownOpening =
        ownComm.commit(resourcePool.getRandomGenerator(), serializer.serialize(value));

    // all parties commit
    List<Commitment> comms = distributeCommitments(ownComm);;

    // all parties send opening info
    List<byte[]> openings = distributeOpenings(ownOpening);

    // open commitments using received opening info
    return open(comms, openings);
  }

  public CommitmentSerializer getCommitmentSerializer() {
    return resourcePool.getCommitmentSerializer();
  }

}
