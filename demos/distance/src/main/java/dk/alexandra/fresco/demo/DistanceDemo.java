/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.demo.helpers.DemoNumericApplication;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.math.BigInteger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

/**
 * A simple demo computing the distance between two secret points
 */
public class DistanceDemo extends DemoNumericApplication<BigInteger> {

  private int myId, myX, myY;

  public DistanceDemo(int id, int x, int y) {
    this.myId = id;
    this.myX = x;
    this.myY = y;
  }

  @Override
  public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric producer) {
    return producer.par(par -> {
      // Input points
      Numeric numericIo = par.numeric();
      DRes<SInt> x1, y1, x2, y2;
      x1 = (myId == 1) ? numericIo.input(BigInteger.valueOf(myX), 1) : numericIo.input(null, 1);
      y1 = (myId == 1) ? numericIo.input(BigInteger.valueOf(myY), 1) : numericIo.input(null, 1);
      x2 = (myId == 2) ? numericIo.input(BigInteger.valueOf(myX), 2) : numericIo.input(null, 2);
      y2 = (myId == 2) ? numericIo.input(BigInteger.valueOf(myY), 2) : numericIo.input(null, 2);
      Pair<DRes<SInt>, DRes<SInt>> input1 = new Pair<>(x1, y1);
      Pair<DRes<SInt>, DRes<SInt>> input2 = new Pair<>(x2, y2);
      Pair<Pair<DRes<SInt>, DRes<SInt>>, Pair<DRes<SInt>, DRes<SInt>>> inputs =
          new Pair<>(input1, input2);
      return () -> inputs;
    }).pairInPar((seq, input) -> {
      Numeric numeric = seq.numeric();
      DRes<SInt> dX = numeric.sub(input.getFirst().getFirst(), input.getSecond().getFirst());
      return numeric.mult(dX, dX);
    } , (seq, input) -> {
      Numeric numeric = seq.numeric();
      DRes<SInt> dY = numeric.sub(input.getFirst().getSecond(), input.getSecond().getSecond());
      return numeric.mult(dY, dY);
    }).seq((seq, distances) -> seq.numeric().add(distances.getFirst(), distances.getSecond()))
        .seq((seq, product) -> seq.numeric().open(product));
  }

  public static <ResourcePoolT extends ResourcePool> void main(String[] args) {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();
    NetworkConfiguration networkConfiguration = null;
    ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric> psConf = null;
    int x, y;
    x = y = 0;
    try {
      cmdUtil.addOption(Option.builder("x").desc("The integer x coordinate of this party. "
          + "Note only party 1 and 2 should supply this input.").hasArg().build());
      cmdUtil.addOption(Option.builder("y").desc("The integer y coordinate of this party. "
          + "Note only party 1 and 2 should supply this input").hasArg().build());
      CommandLine cmd = cmdUtil.parse(args);
      networkConfiguration = cmdUtil.getNetworkConfiguration();
      psConf = cmdUtil.getProtocolSuite();

      if (networkConfiguration.getMyId() == 1 || networkConfiguration.getMyId() == 2) {
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
    DistanceDemo distDemo = new DistanceDemo(networkConfiguration.getMyId(), x, y);
    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce =
        SCEFactory.getSCEFromConfiguration(psConf, cmdUtil.getEvaluator());
    try {
      ResourcePoolT resourcePool = cmdUtil.getResourcePool();
      BigInteger bigInteger = sce.runApplication(distDemo, resourcePool);
      resourcePool.getNetwork().close();
      double dist = Math.sqrt(bigInteger.doubleValue());
      System.out.println("Distance between party 1 and 2 is: " + dist);
    } catch (Exception e) {
      System.out.println("Error while doing MPC: " + e.getMessage());
      e.printStackTrace();
      System.exit(-1);
    } finally {
      sce.shutdownSCE();
    }
  }
}
