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
package dk.alexandra.fresco.suite.tinytables.online;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.LegacyBinaryBuilder;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesANDProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesCloseProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesNOTProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesOpenToAllProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesXORProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This protocol suite is intended for the online phase of the TinyTables
 * protocol as created by Ivan Damgård, Jesper Buus Nielsen and Michael Nielsen
 * from the Department of Computer Science at Aarhus University.
 * </p>
 *
 * <p>
 * When evaluating a protocol in the online phase, it is assumed that the same
 * protocol has been evaluated in the preprocessing phase (see
 * {@link TinyTablesPreproProtocolSuite}), and that all protocols/gates are
 * evaluated in the exact same order. In the preprocessing phase, the two
 * players picked their additive shares of the masks for all wires. In the
 * online phase, the players add actual input values to their share of the mask,
 * and evaluate the protocol. The details on how this is done can be seen in the
 * specific protocols: {@link TinyTablesANDProtocol},
 * {@link TinyTablesCloseProtocol}, {@link TinyTablesNOTProtocol},
 * {@link TinyTablesOpenToAllProtocol} and {@link TinyTablesXORProtocol}.
 * </p>
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class TinyTablesProtocolSuite implements
    ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> {

  private final File tinyTablesFile;
  private TinyTablesStorage storage;
  private static volatile Map<Integer, TinyTablesProtocolSuite> instances = new HashMap<>();
  private final static Logger logger = LoggerFactory.getLogger(TinyTablesProtocolSuite.class);

  public static TinyTablesProtocolSuite getInstance(int id) {
    return instances.get(id);
  }

  public TinyTablesProtocolSuite(int id, File tinyTablesFile) {
    this.tinyTablesFile = tinyTablesFile;
    instances.put(id, this);
  }

  @Override
  public BuilderFactory<ProtocolBuilderBinary> init(ResourcePoolImpl resourcePool) {
    try {
      this.storage = loadTinyTables(tinyTablesFile);
    } catch (ClassNotFoundException ignored) {
    } catch (IOException e) {
      logger.error("Failed to load TinyTables: " + e.getMessage());
    }
    return new LegacyBinaryBuilder(new TinyTablesFactory());
  }

  private TinyTablesStorage loadTinyTables(File file) throws IOException,
      ClassNotFoundException {
    FileInputStream fin = new FileInputStream(file);
    ObjectInputStream is = new ObjectInputStream(fin);
    logger.info("Loading TinyTabels from " + file);
    TinyTablesStorage storage = (TinyTablesStorage) is.readObject();
    is.close();
    return storage;
  }

  public TinyTablesStorage getStorage() {
    return this.storage;
  }

  @Override
  public RoundSynchronization<ResourcePoolImpl> createRoundSynchronization() {
    return new DummyRoundSynchronization<ResourcePoolImpl>();
  }

  @Override
  public ResourcePoolImpl createResourcePool(int myId, int size, Network network, Random rand,
      SecureRandom secRand) {
    return new ResourcePoolImpl(myId, size, network, rand, secRand);
  }

}
