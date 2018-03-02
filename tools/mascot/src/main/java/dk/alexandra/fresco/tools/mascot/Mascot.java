package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.arithm.Addable;
import dk.alexandra.fresco.tools.mascot.bit.BitConverter;
import dk.alexandra.fresco.tools.mascot.cointossing.CoinTossingMpc;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.InputMask;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import dk.alexandra.fresco.tools.mascot.online.OnlinePhase;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import dk.alexandra.fresco.tools.mascot.triple.TripleGeneration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of the main MASCOT protocol (<a href="https://eprint.iacr.org/2016/505.pdf">https://eprint.iacr.org/2016/505.pdf</a>)
 * which can be used for the SPDZ pre-processing phase. <br> Supports generation of multiplication
 * triples, random authenticated elements, and random authenticated bits.
 */
public class Mascot {

  private final TripleGeneration tripleGeneration;
  private final ElementGeneration elementGeneration;
  private final BitConverter bitConverter;
  private final MascotResourcePool resourcePool;

  /**
   * Creates new {@link Mascot}.
   */
  public Mascot(MascotResourcePool resourcePool, Network network, FieldElement macKeyShare) {
    this.resourcePool = resourcePool;
    // agree on joint seed
    StrictBitVector jointSeed = new CoinTossingMpc(resourcePool, network)
        .generateJointSeed(resourcePool.getPrgSeedLength());
    FieldElementPrg jointSampler = new FieldElementPrgImpl(jointSeed);
    this.elementGeneration =
        new ElementGeneration(resourcePool, network, macKeyShare, jointSampler);
    this.tripleGeneration =
        new TripleGeneration(resourcePool, network, elementGeneration, jointSampler);
    this.bitConverter = new BitConverter(resourcePool,
        new OnlinePhase(resourcePool, tripleGeneration, elementGeneration,
            macKeyShare), macKeyShare);
  }

  /**
   * Generates a batch of multiplication triples.
   *
   * @param numTriples number of triples in batch
   * @return multiplication triples
   */
  public List<MultiplicationTriple> getTriples(int numTriples) {
    return tripleGeneration.triple(numTriples);
  }

  /**
   * Runs the input functionality on a batch of field elements. <br> Allows a party to turn
   * unauthenticated, private field elements into a secret-shared authenticated elements. <br> The
   * party holding the input elements should call this method.
   *
   * @param rawElements field elements to input
   * @return this party's authenticated shares of the inputs
   */
  public List<AuthenticatedElement> input(List<FieldElement> rawElements) {
    return elementGeneration.input(rawElements);
  }

  /**
   * Same as {@link #input(List)} but to be called by non-input parties.
   *
   * @param inputterId the id of the inputter
   * @param numElements number of input elements
   * @return this party's authenticated shares of the inputs
   */
  public List<AuthenticatedElement> input(Integer inputterId, int numElements) {
    return elementGeneration.input(inputterId, numElements);
  }

  /**
   * Creates random authenticated elements.
   *
   * @param numElements number of elements to create
   * @return random authenticated elements
   */
  public List<AuthenticatedElement> getRandomElements(int numElements) {
    List<List<AuthenticatedElement>> perPartyElements = new ArrayList<>(
        resourcePool.getNoOfParties());
    for (int partyId = 1; partyId <= resourcePool.getNoOfParties(); partyId++) {
      if (partyId == resourcePool.getMyId()) {
        List<FieldElement> randomElements = resourcePool.getLocalSampler()
            .getNext(resourcePool.getModulus(), numElements);
        perPartyElements.add(elementGeneration.input(randomElements));
      } else {
        perPartyElements.add(elementGeneration.input(partyId, numElements));
      }
    }
    return Addable.sumRows(perPartyElements);
  }

  /**
   * Generates random input masks.
   *
   * @param maskerId the party that knows the plain mask
   * @param numMasks number of masks to generate
   * @return input masks
   */
  public List<InputMask> getInputMasks(Integer maskerId, int numMasks) {
    if (maskerId.equals(resourcePool.getMyId())) {
      List<FieldElement> randomMasks = resourcePool.getLocalSampler()
          .getNext(resourcePool.getModulus(), numMasks);
      List<AuthenticatedElement> authenticated = input(randomMasks);
      return IntStream.range(0, numMasks)
          .mapToObj(idx -> new InputMask(randomMasks.get(idx), authenticated.get(idx)))
          .collect(Collectors.toList());
    } else {
      return input(maskerId, numMasks).stream().map(InputMask::new)
          .collect(Collectors.toList());
    }
  }

  /**
   * Generates random bits (as authenticated elements).
   *
   * @param numBits number of bits to generate
   * @return random bits
   */
  public List<AuthenticatedElement> getRandomBits(int numBits) {
    return bitConverter.convertToBits(getRandomElements(numBits));
  }

}
