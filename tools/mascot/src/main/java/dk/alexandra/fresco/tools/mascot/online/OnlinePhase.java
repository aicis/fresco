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

public class OnlinePhase extends BaseProtocol {

  private final TripleGeneration tripleGeneration;
  private final ElementGeneration elementGeneration;
  private final FieldElement macKeyShare;

  /**
   * Creates new {@link OnlinePhase}.
   *
   * @param resourcePool mascot resource pool
   * @param network network
   */
  public OnlinePhase(MascotResourcePool resourcePool,
      Network network, TripleGeneration tripleGeneration, ElementGeneration elementGeneration,
      FieldElement macKeyShare) {
    super(resourcePool, network);
    this.tripleGeneration = tripleGeneration;
    this.elementGeneration = elementGeneration;
    this.macKeyShare = macKeyShare;
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
    // TODO do these values need to be checked when online output protocol is invoked?
    List<FieldElement> openEpsilons = elementGeneration.open(epsilons);
    List<FieldElement> openDeltas = elementGeneration.open(deltas);
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

}
