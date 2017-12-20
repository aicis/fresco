package dk.alexandra.fresco.tools.mascot.commit;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.SecureSerializer;
import dk.alexandra.fresco.tools.commitment.Commitment;
import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.broadcast.BroadcastValidation;
import dk.alexandra.fresco.tools.mascot.broadcast.BroadcastingNetworkDecorator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Actively-secure protocol for binding input. Allows each party to distribute a value to the other
 * parties using commitments.
 * 
 * @param <T> type of value to commit to
 */
public class CommitmentBasedInput<T> extends BaseProtocol {

  private final SecureSerializer<T> serializer;
  private final Network broadcaster;

  /**
   * Creates new {@link CommitmentBasedInput}.
   */
  public CommitmentBasedInput(MascotResourcePool resourcePool, Network network,
      SecureSerializer<T> serializer) {
    super(resourcePool, network);
    this.serializer = serializer;
    // for more than two parties, we need to use broadcast
    if (resourcePool.getNoOfParties() > 2) {
      this.broadcaster =
          new BroadcastingNetworkDecorator(network, new BroadcastValidation(resourcePool, network));
    } else {
      // if we have two parties or less we can just use the regular network
      this.broadcaster = this.getNetwork();
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
        .map(raw -> getCommitmentSerializer().deserialize(raw)).collect(Collectors.toList());
    return comms;
  }

  /**
   * Sends own opening info to others and receives others' opening info.
   *
   * @param opening own opening info
   */
  protected List<byte[]> distributeOpenings(byte[] opening) {
    // send (over regular network) own opening info
    getNetwork().sendToAll(opening);
    // receive opening info from others
    List<byte[]> openings = getNetwork().receiveFromAll();
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

  /**
   * Uses commitments to securely distribute the given value to the other parties and receive their
   * inputs.
   * 
   * @param value value to commit to
   * @return the other parties' values
   */
  protected List<T> allCommit(T value) {
    // commit to sigma
    Commitment ownComm = new Commitment();

    // commit to value locally
    byte[] ownOpening = ownComm.commit(getRandomGenerator(), serializer.serialize(value));

    // all parties commit
    List<Commitment> comms = distributeCommitments(ownComm);;

    // all parties send opening info
    List<byte[]> openings = distributeOpenings(ownOpening);

    // open commitments using received opening info
    return open(comms, openings);
  }

}
