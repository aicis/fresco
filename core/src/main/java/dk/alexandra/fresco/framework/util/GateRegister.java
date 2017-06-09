/*
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
package dk.alexandra.fresco.framework.util;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import java.util.HashMap;
import java.util.LinkedList;

public class GateRegister {

  private final static HashMap<NativeProtocol, LinkedList<ProtocolProducer>> gateMap = new HashMap<NativeProtocol, LinkedList<ProtocolProducer>>();
  private static final boolean record = false;

  private GateRegister() {
  }

  ;

  public static boolean isRecording() {
    return record;
  }

  private static void registerGate(NativeProtocol g, ProtocolProducer p) {
    synchronized (gateMap) {
      if (!record) {
        return;
      }
      if (gateMap.containsKey(g)) {
        LinkedList<ProtocolProducer> list = gateMap.get(g);
        list.add(p);
      } else {
        LinkedList<ProtocolProducer> list = new LinkedList<ProtocolProducer>();
        list.add(p);
        gateMap.put(g, list);
      }
    }
  }

  public static void registerGates(NativeProtocol[] gs, ProtocolProducer p) {
    if (!record) {
      return;
    }
    synchronized (gateMap) {
      for (NativeProtocol g : gs) {
        registerGate(g, p);
      }
    }
  }

  public static void registerGates(NativeProtocol[] gs, int start, int end, ProtocolProducer p) {
    if (!record) {
      return;
    }
    synchronized (gateMap) {
      for (int i = start; i < end; i++) {
        registerGate(gs[i], p);
      }
    }
  }

  public static void reset() {
    if (!record) {
      return;
    }
    synchronized (gateMap) {
      gateMap.clear();
    }
  }

  public static LinkedList<ProtocolProducer> lookUp(NativeProtocol g) {
    if (!record) {
      return null;
    }
    synchronized (gateMap) {
      return gateMap.get(g);
    }
  }

  public static String gateToString(NativeProtocol g) {
    if (!record) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(g.getClass());
    synchronized (gateMap) {
      if (gateMap.containsKey(g)) {
        for (ProtocolProducer gp : gateMap.get(g)) {
          sb.append(" ->\n");
          sb.append(gp.getClass().toString());
        }
        return sb.toString();
      } else {
        return null;
      }
    }
  }

}
