package dk.alexandra.fresco.tools.bitTriples.bracket;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.BitTripleResourcePool;
import dk.alexandra.fresco.tools.bitTriples.cote.CoteInstances;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrg;
import dk.alexandra.fresco.tools.bitTriples.utils.VectorOperations;
import dk.alexandra.fresco.tools.ot.otextension.CoteFactory;
import java.util.ArrayList;
import java.util.List;

public class Bracket {

  private final Network network;
  private final BitTripleResourcePool resourcePool;
  private final CoteInstances COTeInstances;
  private final BytePrg jointSampler;
  private final StrictBitVector mac;

  /**
   * Implements the []-protocol described in Figure 6.
   *  @param resourcePool The resource pool
   * @param network The network
   * @param jointSampler
   */
  public Bracket(BitTripleResourcePool resourcePool, Network network, BytePrg jointSampler) {
    this(resourcePool, network, resourcePool.getLocalSampler().getNext(resourcePool.getComputationalSecurityBitParameter()), jointSampler);
  }

  public Bracket(BitTripleResourcePool resourcePool, Network network, StrictBitVector mac, BytePrg jointSampler) {
    this.resourcePool = resourcePool;
    this.network = network;
    this.jointSampler = jointSampler;
    this.mac = mac;
    COTeInstances = new CoteInstances(resourcePool,network,mac);
  }

  public List<StrictBitVector> input(int amountOfElements) {
    // Sample random input
    StrictBitVector randomInput = resourcePool.getLocalSampler().getNext(amountOfElements);
    return this.input(randomInput);
  }

  public List<StrictBitVector> input(StrictBitVector input) {
    // Step 1. n-share to obtain shares
    List<StrictBitVector> shares = nShare(input);
    // Step 2. broadcast own shares
    List<StrictBitVector> receivedShares =
        VectorOperations.distributeVector(input, resourcePool, network);
    receivedShares.add(input);
    StrictBitVector sumOfAllShares = VectorOperations.bitwiseXor(receivedShares);
    // Step 3. Check macs
    MacCheckShares macCheckShares = new MacCheckShares(resourcePool, network, jointSampler);

    macCheckShares.check(sumOfAllShares, shares, mac);

    return shares;
  }

  /**
   * Runs n-Share described in Figure 6 and 7. where every party randomly pick a number of shares as
   * input.
   *
   * @param myInput the choice bits as an input vector x^(i)_1,...,x^(i)_k
   * @return
   */
  protected List<StrictBitVector> nShare(StrictBitVector myInput) {
    List<List<StrictBitVector>> sharesWithMac = new ArrayList<>();
    for (int receiverId = 1; receiverId <= resourcePool.getNoOfParties(); receiverId++) {
      List<StrictBitVector> resultFromShare = share(receiverId, myInput);
      sharesWithMac.add(resultFromShare);
    }

    return VectorOperations.xorMatchingIndices(sharesWithMac, myInput.getSize());
  }

  /**
   * shares input among the parties
   *
   * @param receiverId party to input
   * @param receiverInput the input
   * @return list of bitvectors - the sum of the vector is the share of all kappa bits.
   */
  protected List<StrictBitVector> share(int receiverId, StrictBitVector receiverInput) {
    List<List<StrictBitVector>> tResults = new ArrayList<>();
    List<StrictBitVector> qs = new ArrayList<>();
    for (int senderId = 1; senderId <= resourcePool.getNoOfParties(); senderId++) {
      if (senderId != receiverId) {
        CoteFactory coteInstance = COTeInstances.get(receiverId,senderId);
        if (resourcePool.getMyId() == senderId) {
          qs = coteInstance.getSender().extend(receiverInput.getSize());
        } else if (resourcePool.getMyId() == receiverId) {
          tResults.add(coteInstance.getReceiver().extend(receiverInput));
        }
      }
    }
    if (resourcePool.getMyId() == receiverId) {
      return constructUs(receiverInput, tResults);
    } else {
      return qs;
    }
  }

  protected List<StrictBitVector> constructUs(
      StrictBitVector receiverInput, List<List<StrictBitVector>> tResults) {
    List<StrictBitVector> us = new ArrayList<>();
    for (int l = 0; l < receiverInput.getSize(); l++) {
      StrictBitVector sumOfTs = VectorOperations.xorIndex(tResults, l);
      if (receiverInput.getBit(l, false)) {
        sumOfTs.xor(mac);
      }
      us.add(sumOfTs);
    }
    return us;
  }
  public CoteInstances getCOTeInstances() {
    return COTeInstances;
  }

}
