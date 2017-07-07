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
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import java.util.Map;
import java.util.logging.Level;

public interface SCEConfiguration<ResourcePoolT extends ResourcePool> {

  int getMyId();

  Map<Integer, Party> getParties();

  /**
   * Defaults to info if logLevel is not found in the properties file.
   */
  Level getLogLevel();

  /**
   * Get the network strategy to use, is only used when getNetwork returns null.
   *
   * @return The network strategy wanted.
   */
  NetworkingStrategy getNetworkStrategy();

  /**
   * Reads the config for an indication on which kind of GateEvaluator should
   * be used.
   */
  ProtocolEvaluator<ResourcePoolT> getEvaluator();

  /**
   * Returns the streamed storage requested.
   */
  StreamedStorage getStreamedStorage();

}
