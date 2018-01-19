package dk.alexandra.fresco.tools.mascot.commit;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.broadcast.BroadcastValidation;
import dk.alexandra.fresco.tools.mascot.broadcast.BroadcastingNetworkProxy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Actively-secure protocol for binding input. Allows each party to distribute a value to the other
 * parties using commitments.
 *
 * @param <T> type of value to commit to
 */
public class CommitmentBasedInput<T> {

  private final ByteSerializer<T> serializer;
  private final Network broadcaster;
  private final MascotResourcePool resourcePool;
  private final Network network;

  /**
   * Creates new {@link CommitmentBasedInput}.
   */
  public CommitmentBasedInput(MascotResourcePool resourcePool, Network network,
      ByteSerializer<T> serializer) {
    this.resourcePool = resourcePool;
    this.network = network;
    this.serializer = serializer;
    // for more than two parties, we need to use broadcast
    if (resourcePool.getNoOfParties() > 2) {
      this.broadcaster =
          new BroadcastingNetworkProxy(network, new BroadcastValidation(resourcePool, network));
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
  private List<HashBasedCommitment> distributeCommitments(HashBasedCommitment comm) {
    // broadcast own commitment
    broadcaster.sendToAll(getResourcePool().getCommitmentSerializer().serialize(comm));
    // receive other parties' commitments from broadcast
    List<byte[]> rawComms = broadcaster.receiveFromAll();
    // parse
    return rawComms.stream()
        .map(getResourcePool().getCommitmentSerializer()::deserialize)
        .collect(Collectors.toList());
  }

  /**
   * Sends own opening info to others and receives others' opening info.
   *
   * @param opening own opening info
   */
  private List<byte[]> distributeOpenings(byte[] opening) {
    // send (over regular network) own opening info
    getNetwork().sendToAll(opening);
    // receive opening info from others
    return getNetwork().receiveFromAll();
  }

  /**
   * Attempts to open commitments using opening info, will throw if opening fails.
   *
   * @param commitments commitments
   * @param openings opening information
   * @return values from opened commitments
   * @throws dk.alexandra.fresco.framework.MaliciousException if opening fails
   */
  protected List<T> open(List<HashBasedCommitment> commitments, List<byte[]> openings) {
    if (commitments.size() != openings.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    List<T> result = new ArrayList<>(commitments.size());
    for (int i = 0; i < commitments.size(); i++) {
      HashBasedCommitment comm = commitments.get(i);
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
    HashBasedCommitment ownComm = new HashBasedCommitment();

    // commit to value locally
    byte[] ownOpening = ownComm
        .commit(getResourcePool().getRandomGenerator(), serializer.serialize(value));

    // all parties commit
    List<HashBasedCommitment> comms = distributeCommitments(ownComm);

    // all parties send opening info
    List<byte[]> openings = distributeOpenings(ownOpening);

    // open commitments using received opening info
    return open(comms, openings);
  }

  private Network getNetwork() {
    return network;
  }

  protected MascotResourcePool getResourcePool() {
    return resourcePool;
  }
}
