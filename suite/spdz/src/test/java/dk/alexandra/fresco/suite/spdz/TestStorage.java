/*******************************************************************************
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
package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.exceptions.NoMoreElementsException;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.utils.Util;
import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestStorage {

  @Before
  public void init() {
    Reporter.init(Level.INFO);
    Util.setModulus(new BigInteger(
        "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329"));
  }

  @Test
  public void testInMemoryStorage() {
    Storage storage = new InMemoryStorage();
    testStorage(storage);
    testStoreBigInteger(storage);
  }


  @Test
  public void testFilebasedStorage() throws NoMoreElementsException {
    StreamedStorage storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
    testStorage(storage);
    testStoreBigInteger(storage);
    testStreamedStorage(storage);
    File f = new File("testName");
    if (f.exists()) {
      f.delete();
    }
  }

  private void testStreamedStorage(StreamedStorage storage) throws NoMoreElementsException {
    storage.putNext("testName", BigInteger.TEN);
    Serializable o = storage.getNext("testName");
    Assert.assertEquals(BigInteger.TEN, o);
  }

  public void testStorage(Storage storage) {
    SpdzElement a = new SpdzElement(BigInteger.ONE, BigInteger.ZERO);
    SpdzTriple o1 = new SpdzTriple(a, a, a);

    storage.putObject("test", "key", o1);

    SpdzTriple o2 = storage.getObject("test", "key");
    Assert.assertEquals(o1, o2);
  }

  public void testStoreBigInteger(Storage storage) {
    BigInteger o1 = BigInteger.ONE;

    storage.putObject("test", "key", o1);

    BigInteger o2 = storage.getObject("test", "key");
    Assert.assertEquals(o1, o2);
  }
}
