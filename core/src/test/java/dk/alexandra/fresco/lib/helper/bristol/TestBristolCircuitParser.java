/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.helper.bristol;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.suite.dummy.DummyFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;
import org.junit.Test;

public class TestBristolCircuitParser {

  @Test
  public void testCircuitParser() throws Exception {
    String path = "circuits/AES-non-expanded.txt";
    InputStream circuit = getClass().getClassLoader().getResourceAsStream(path);
    Stream<String> lines = new BufferedReader(new InputStreamReader(circuit)).lines();

    BasicLogicFactory boolFac = new DummyFactory();

    // Some plaintext input.
    boolean[] in1_vals = new boolean[128];
    boolean[] in2_vals = new boolean[128];
    for (int i = 0; i < 128; i++) {
      in1_vals[i] = true;
      in2_vals[i] = true;
    }

    SBool[] in1 = boolFac.getKnownConstantSBools(in1_vals);
    SBool[] in2 = boolFac.getKnownConstantSBools(in2_vals);
    SBool[] out = boolFac.getSBools(128);
    BristolCircuitParser cp = new BristolCircuitParser(lines, boolFac, in1, in2, out);

    assertEquals(33872, cp.getNoOfWires());
    ProtocolProducer[] c = new ProtocolProducer[500]; // More than enough.
    int i = 0;
    int[] size = new int[3];
    while (i < 3) {
      size[i++] = cp.getNext(c, 0);
      //System.out.println("Read " + size[i] + " circuits");
    }

    // The first layer in the AES circuit consist of exactly 168 gates.
    assertEquals(168, size[0]);
    assertEquals(0, size[1]);
    assertEquals(0, size[2]);

  }

}
