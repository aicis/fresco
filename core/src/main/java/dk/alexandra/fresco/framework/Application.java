/*
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
package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;

/**
 * The Application interface should be implemented by all MPC applications that should run within
 * FRESCO. Application developers choose between the different type of builders and the output type
 * the application should produce. The developer is then provided with a method to fill out and a
 * builder to use of the specified type. This builder can then be used to construct the application.
 * 
 * @param <OutputT> The output type
 * @param <Builder> The builder type (i.e. currently either binary or arithmetic)
 */
public interface Application<OutputT, Builder extends ProtocolBuilder>
    extends Computation<OutputT, Builder> {

  /**
   * Closes the application and allows the output to be produced and allocated resources to be
   * released.
   */
  default void close() {

  }
}
