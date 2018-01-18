package dk.alexandra.fresco.tools.mascot.online;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.mascot.triple.TripleGeneration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class OnlinePhase extends BaseProtocol {

  private final TripleGeneration tripleGeneration;
  private final ElementGeneration elementGeneration;
  private final FieldElement macKeyShare;
  private final OpenedValueStore openedValueStore;

  /**
   * Creates new {@link OnlinePhase}.
   */
  public OnlinePhase(MascotResourcePool resourcePool,
      Network network, TripleGeneration tripleGeneration, ElementGeneration elementGeneration,
      FieldElement macKeyShare) {
    super(resourcePool, network);
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
          .add(left.multiply(delta)).add(epsilonDeltaProd, getMyId(), macKeyShare);
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

  /**
   * A class that stores all opened values along with their macs and runs mac checks when required.
   */
  private class OpenedValueStore {

    private final List<AuthenticatedElement> sharesWithMacs;
    private final List<FieldElement> openedValues;

    public OpenedValueStore() {
      this.sharesWithMacs = new ArrayList<>();
      this.openedValues = new ArrayList<>();
    }

    public void addValues(List<AuthenticatedElement> newSharesWithMacs,
        List<FieldElement> newOpenedValues) {
      this.sharesWithMacs.addAll(newSharesWithMacs);
      this.openedValues.addAll(newOpenedValues);
    }

    public void checkAllAndClear(
        BiConsumer<List<AuthenticatedElement>, List<FieldElement>> checker) {
      checker.accept(this.sharesWithMacs, this.openedValues);
      this.sharesWithMacs.clear();
      this.openedValues.clear();
    }

  }

}
