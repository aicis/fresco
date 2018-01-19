package dk.alexandra.fresco.tools.mascot.online;

import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.mascot.triple.TripleGeneration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * This class contains subset of the online SPDZ protocols. including the online multiplication
 * protocol. <p>This functionality is needed in the offline for generating random bits as
 * pre-processed material but may also be used as a light-weight online SPDZ runtime.</p>
 */
public class OnlinePhase {

  private final TripleGeneration tripleGeneration;
  private final ElementGeneration elementGeneration;
  private final FieldElement macKeyShare;
  private final OpenedValueStore openedValueStore;
  private final MascotResourcePool resourcePool;

  /**
   * Creates new {@link OnlinePhase}.
   */
  public OnlinePhase(MascotResourcePool resourcePool, TripleGeneration tripleGeneration,
      ElementGeneration elementGeneration,
      FieldElement macKeyShare) {
    this.resourcePool = resourcePool;
    this.tripleGeneration = tripleGeneration;
    this.elementGeneration = elementGeneration;
    this.macKeyShare = macKeyShare;
    this.openedValueStore = new OpenedValueStore();
  }

  /**
   * Performs an "online" multiplication protocol, i.e., computes authenticated products of left and
   * right factors.
   *
   * @param leftFactors left factors
   * @param rightFactors right factors
   * @return authenticated products
   */
  public List<AuthenticatedElement> multiply(List<AuthenticatedElement> leftFactors,
      List<AuthenticatedElement> rightFactors) {
    List<MultTriple> triples = tripleGeneration.triple(leftFactors.size());
    List<AuthenticatedElement> epsilons = new ArrayList<>(leftFactors.size());
    List<AuthenticatedElement> deltas = new ArrayList<>(leftFactors.size());
    for (int i = 0; i < leftFactors.size(); i++) {
      AuthenticatedElement left = leftFactors.get(i);
      AuthenticatedElement right = rightFactors.get(i);
      MultTriple triple = triples.get(i);
      epsilons.add(left.subtract(triple.getLeft()));
      deltas.add(right.subtract(triple.getRight()));
    }
    List<FieldElement> openEpsilons = open(epsilons);
    List<FieldElement> openDeltas = open(deltas);
    List<AuthenticatedElement> products = new ArrayList<>(leftFactors.size());
    for (int i = 0; i < leftFactors.size(); i++) {
      MultTriple triple = triples.get(i);
      AuthenticatedElement left = triple.getLeft();
      AuthenticatedElement right = triple.getRight();
      FieldElement epsilon = openEpsilons.get(i);
      FieldElement delta = openDeltas.get(i);
      FieldElement epsilonDeltaProd = epsilon.multiply(delta);
      // [c] + epsilon * [b] + delta * [a] + epsilon * delta
      AuthenticatedElement product = triple.getProduct().add(right.multiply(epsilon))
          .add(left.multiply(delta))
          .add(epsilonDeltaProd, getResourcePool().getMyId(), macKeyShare);
      products.add(product);
    }
    return products;
  }

  /**
   * Runs open protocol and stores all opened values along with the macs for later validation.
   */
  public List<FieldElement> open(List<AuthenticatedElement> closed) {
    List<FieldElement> opened = elementGeneration.open(closed);
    openedValueStore.addValues(closed, opened);
    return opened;
  }

  /**
   * Manual way to initialize mac check on all unchecked values opened so far.
   */
  public void triggerMacCheck() {
    openedValueStore.checkAllAndClear(elementGeneration::check);
  }

  private MascotResourcePool getResourcePool() {
    return resourcePool;
  }

  /**
   * A class that stores all opened values along with their macs and runs mac checks when required.
   */
  private class OpenedValueStore {

    private final List<AuthenticatedElement> sharesWithMacs;
    private final List<FieldElement> openedValues;

    OpenedValueStore() {
      this.sharesWithMacs = new ArrayList<>();
      this.openedValues = new ArrayList<>();
    }

    void addValues(List<AuthenticatedElement> newSharesWithMacs,
        List<FieldElement> newOpenedValues) {
      this.sharesWithMacs.addAll(newSharesWithMacs);
      this.openedValues.addAll(newOpenedValues);
    }

    void checkAllAndClear(
        BiConsumer<List<AuthenticatedElement>, List<FieldElement>> checker) {
      checker.accept(this.sharesWithMacs, this.openedValues);
      this.sharesWithMacs.clear();
      this.openedValues.clear();
    }

  }

}
