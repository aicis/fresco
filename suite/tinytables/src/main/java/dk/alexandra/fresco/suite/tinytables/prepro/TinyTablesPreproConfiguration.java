/*
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.tinytables.prepro;

import java.io.File;
import java.security.SecureRandom;
import java.util.Random;

import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class TinyTablesPreproConfiguration implements ProtocolSuiteConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> {
  
  private File tinytablesfile;
  private int triplesBatchSize;

  public TinyTablesPreproConfiguration() {
  }

  /**
   * Set file where generated TinyTables should be stored. Each AND gate need
   * one TinyTable, and a TinyTable should only be used once in the online
   * phase
   */
  public void setTinyTablesFile(File file) {
    this.tinytablesfile = file;
  }

  /**
   * Get the file where TinyTables are stored to.
   */
  File getTinyTablesFile() {
    return this.tinytablesfile;
  }

  /**
   * Set the number of triples that we want to load at a time. Decreasing this
   * will use less memory but increasing it will decrease the number of times
   * we need to generate and load triples.
   */
  public void setTriplesBatchSize(int batchSize) {
    this.triplesBatchSize = batchSize;
  }

  /**
   * Get the number of triples loaded at a time.
   */
  public int getTriplesBatchSize() {
    return this.triplesBatchSize;
  }

  @Override
  public ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> createProtocolSuite(int myPlayerId) {
    return new TinyTablesPreproProtocolSuite(myPlayerId, this);
  }

  @Override
  public ResourcePoolImpl createResourcePool(int myId, int size, Network network, Random rand,
      SecureRandom secRand) {
    return new ResourcePoolImpl(myId, size, network, rand, secRand);
  }

}
