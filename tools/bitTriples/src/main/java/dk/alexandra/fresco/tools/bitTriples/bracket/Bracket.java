package dk.alexandra.fresco.tools.bitTriples.bracket;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.BitTripleResourcePool;
import dk.alexandra.fresco.tools.bitTriples.cote.CoteInstances;
import dk.alexandra.fresco.tools.bitTriples.maccheck.MacCheck;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrg;
import dk.alexandra.fresco.tools.bitTriples.utils.VectorOperations;
import dk.alexandra.fresco.tools.ot.otextension.CoteFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Actively-secure protocol for authenticating secret shared bits (described in fig. 6).
 */
public class Bracket {

  private final Network network;
  private final BitTripleResourcePool resourcePool;
  private final CoteInstances COTeInstances;
  private final BytePrg jointSampler;
  private final StrictBitVector mac;

  /**
   * Create a new []-protocol.
   * @param resourcePool The resource pool
   * @param network The network
   * @param jointSampler a sampler constructed with a joint seed
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

  /**
   * Constructs a sharing for a set of random elements.
   * @param amountOfElements the amount of elements
   * @return a list of sharings
   */

  public List<StrictBitVector> input(int amountOfElements) {
    // Sample random input
    StrictBitVector randomInput = resourcePool.getLocalSampler().getNext(amountOfElements);
    return input(randomInput);
  }

  /**
   * Constructs a sharing for the given elements.
   * @param input input elements
   * @return a list of sharings
   */

  public List<StrictBitVector> input(StrictBitVector input) {
    // Step 1. n-share to obtain shares
    List<StrictBitVector> shares = nShare(input);
    // Step 2. broadcast own input
    StrictBitVector openInput = VectorOperations.openVector(input,resourcePool,network);
    // Step 3. Check macs
    MacCheck macCheckShares = new MacCheck(resourcePool, network, jointSampler);

    macCheckShares.check(openInput, shares, mac);

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

    return VectorOperations.sumMatchingIndices(sharesWithMac, myInput.getSize());
  }

  /**
   * shares input among the parties
   *
   * @param receiver party to input
   * @param receiverInput the input
   * @return list of bitvectors - the sum of the vector is the share of all kappa bits.
   */
  protected List<StrictBitVector> share(int receiver, StrictBitVector receiverInput) {
    List<List<StrictBitVector>> tResults = new ArrayList<>();
    List<StrictBitVector> qs = new ArrayList<>();
    for (int sender = 1; sender <= resourcePool.getNoOfParties(); sender++) {
      if (sender != receiver) {
        CoteFactory instance = COTeInstances.get(receiver,sender);
        if (resourcePool.getMyId() == sender) {
          qs = instance.getSender().extend(receiverInput.getSize());
        } else if (resourcePool.getMyId() == receiver) {
          tResults.add(instance.getReceiver().extend(receiverInput));
        }
      }
    }
    if (resourcePool.getMyId() == receiver) {
      return constructUs(receiverInput, tResults);
    } else {
      return qs;
    }
  }

  /**
   * Constructs the <i>u</i>'s describes in Fig. 7 step 2.
   * @param receiverInput the input of the receiving party
   * @param tResults the <i>t</i>'s resulting from extending COTe
   * @return the list of <i>u</i>'s
   */
  protected List<StrictBitVector> constructUs(
      StrictBitVector receiverInput, List<List<StrictBitVector>> tResults) {
    List<StrictBitVector> us = new ArrayList<>();
    for (int l = 0; l < receiverInput.getSize(); l++) {
      StrictBitVector sumOfTs = VectorOperations.xorAtIndex(tResults, l);
      if (receiverInput.getBit(l, false)) {
        sumOfTs.xor(mac);
      }
      us.add(sumOfTs);
    }
    return us;
  }

  /**
   * Returns the COTe instances used in this protocol
   * @return COTe instances
   */
  public CoteInstances getCOTeInstances() {
    return COTeInstances;
  }

}
