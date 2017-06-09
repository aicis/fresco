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
package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

/**
 * A simple demo computing the distance between two secret points
 */
public class DistanceDemo implements Application {

  private static final long serialVersionUID = 6415583508947017554L;

  private int myId, myX, myY;
  private OInt distance;

  private DistanceDemo(int id, int x, int y) {
    this.myId = id;
    this.myX = x;
    this.myY = y;
  }

  @Override
  public ProtocolProducer prepareApplication(ProtocolFactory factory) {
    BasicNumericFactory bnFac = (BasicNumericFactory) factory;
    NumericProtocolBuilder npb = new NumericProtocolBuilder(bnFac);
    NumericIOBuilder iob = new NumericIOBuilder(bnFac);
    // Input points
    iob.beginParScope();
    SInt x1 = (myId == 1) ? iob.input(myX, 1) : iob.input(1);
    SInt y1 = (myId == 1) ? iob.input(myY, 1) : iob.input(1);
    SInt x2 = (myId == 2) ? iob.input(myX, 2) : iob.input(2);
    SInt y2 = (myId == 2) ? iob.input(myY, 2) : iob.input(2);
    iob.endCurScope();
    // Compute distance squared (note, square root computation can be done publicly)
    npb.beginParScope();
    npb.beginSeqScope();
    SInt dX = npb.sub(x1, x2);
    SInt sqDX = npb.mult(dX, dX);
    npb.endCurScope();
    npb.beginSeqScope();
    SInt dY = npb.sub(y1, y2);
    SInt sqDY = npb.mult(dY, dY);
    npb.endCurScope();
    npb.endCurScope();
    SInt result = npb.add(sqDX, sqDY);
    iob.addProtocolProducer(npb.getProtocol());
    // Output result
    distance = iob.output(result);
    return iob.getProtocol();
  }

  public static void main(String[] args) {
    CmdLineUtil cmdUtil = new CmdLineUtil();
    SCEConfiguration sceConf = null;
    int x, y;
    x = y = 0;
    try {
      cmdUtil.addOption(Option.builder("x")
          .desc("The integer x coordinate of this party. "
              + "Note only party 1 and 2 should supply this input.")
          .hasArg()
          .build());
      cmdUtil.addOption(Option.builder("y")
          .desc("The integer y coordinate of this party. "
              + "Note only party 1 and 2 should supply this input")
          .hasArg()
          .build());
      CommandLine cmd = cmdUtil.parse(args);
      sceConf = cmdUtil.getSCEConfiguration();

      if (sceConf.getMyId() == 1 || sceConf.getMyId() == 2) {
        if (!cmd.hasOption("x") || !cmd.hasOption("y")) {
          throw new ParseException("Party 1 and 2 must submit input");
        } else {
          x = Integer.parseInt(cmd.getOptionValue("x"));
          y = Integer.parseInt(cmd.getOptionValue("y"));
        }
      } else {
        if (cmd.hasOption("x") || cmd.hasOption("y")) {
          throw new ParseException("Only party 1 and 2 should submit input");
        }
      }

    } catch (ParseException | IllegalArgumentException e) {
      System.out.println("Error: " + e);
      System.out.println();
      cmdUtil.displayHelp();
      System.exit(-1);
    }
    DistanceDemo distDemo = new DistanceDemo(sceConf.getMyId(), x, y);
    dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration psConf = cmdUtil
        .getProtocolSuiteConfiguration();
    SecureComputationEngine sce = new SecureComputationEngineImpl(sceConf, psConf);
    try {
      sce.runApplication(distDemo, SecureComputationEngineImpl.createResourcePool(sceConf));
    } catch (Exception e) {
      System.out.println("Error while doing MPC: " + e.getMessage());
      e.printStackTrace();
      System.exit(-1);
    }
    double dist = distDemo.distance.getValue().doubleValue();
    dist = Math.sqrt(dist);
    System.out.println("Distance between party 1 and 2 is " + dist);
  }

}
