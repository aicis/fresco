/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.framework.sce.configuration;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.util.HashMap;
import java.util.Map;

public class TestSCEConfiguration<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> implements
    SCEConfiguration<ResourcePoolT> {

  private NetworkingStrategy network;
  private Map<Integer, Party> parties;
  private int myId;
  private ProtocolEvaluator<ResourcePoolT> evaluator;
  private final ProtocolSuite<ResourcePoolT, Builder> suite;

  public TestSCEConfiguration(ProtocolSuite<ResourcePoolT, Builder> suite,
      NetworkingStrategy network,
      ProtocolEvaluator<ResourcePoolT> evaluator,
      NetworkConfiguration conf,
      boolean useSecureConn) {
    this.suite = suite;
    this.network = network;
    this.evaluator = evaluator;
    evaluator.setMaxBatchSize(4096);
    this.myId = conf.getMyId();
    parties = new HashMap<>();
    for (int i = 1; i <= conf.noOfParties(); i++) {
      if (useSecureConn) {
        Party p = conf.getParty(i);
        //Use the same hardcoded test 128 bit AES key for all connections
        p.setSecretSharedKey("w+1qn2ooNMCN7am9YmYQFQ==");
        parties.put(i, p);
      } else {
        parties.put(i, conf.getParty(i));
      }
    }
  }

  public ProtocolSuite<ResourcePoolT, Builder> getSuite() {
    return suite;
  }


  @Override
  public int getMyId() {
    return myId;
  }

  @Override
  public Map<Integer, Party> getParties() {
    return parties;
  }

  @Override
  public ProtocolEvaluator<ResourcePoolT> getEvaluator() {
    return this.evaluator;
  }

  public NetworkingStrategy getNetworkStrategy() {
    return this.network;
  }

}
