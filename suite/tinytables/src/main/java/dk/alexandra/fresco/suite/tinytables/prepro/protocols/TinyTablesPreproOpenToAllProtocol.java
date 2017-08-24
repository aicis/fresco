/*
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.suite.tinytables.prepro.protocols;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;

/**
 * <p>
 * This class represents an open-to-all protocol in the preprocessing phase of the TinyTables
 * protocol.
 * </p>
 * 
 * <p>
 * Here each player stores his share of the mask of the input wire. In the online phase this is then
 * shared with the other player to allow the unmasking of the input value.
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesPreproOpenToAllProtocol extends TinyTablesPreproProtocol<Void> {

  private Computation<SBool> toOpen;

  public TinyTablesPreproOpenToAllProtocol(int id, Computation<SBool> toOpen) {
    super();
    this.id = id;
    this.toOpen = toOpen;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolImpl resourcePool, SCENetwork network) {
    TinyTablesPreproProtocolSuite ps =
        TinyTablesPreproProtocolSuite.getInstance(resourcePool.getMyId());

    /*
     * To open a value, we use the same mask share as for the input which will cancel out the mask
     * when the shares are combined.
     */
    ps.getStorage().storeMaskShare(id, ((TinyTablesPreproSBool) toOpen.out()).getValue());

    return EvaluationStatus.IS_DONE;
  }

  @Override
  public Void out() {
    return null;
  }

}
