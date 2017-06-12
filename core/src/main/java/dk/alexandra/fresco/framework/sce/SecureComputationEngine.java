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
package dk.alexandra.fresco.framework.sce;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.io.IOException;
import java.util.concurrent.Future;

public interface SecureComputationEngine<ResourcePoolT extends ResourcePool> {

  /**
   * Executes an application based on the current SCEConfiguration. If the SecureComputationEngine
   * is not setup before (e.g. connected to other parties etc.), the SecureComputationEngine will
   * do the setup phase before running the application.
   *
   * @param application The application to evaluate.
   */
  void runApplication(Application application, ResourcePoolT resources);

  /**
   * Executes an application based on the current SCEConfiguration. If the SecureComputationEngine
   * is not setup before (e.g. connected to other parties etc.), the SecureComputationEngine will
   * do the setup phase before running the application.
   *
   * @param application The application to evaluate.
   */
  Future<?> startApplication(Application application, ResourcePoolT resources);


  /**
   * Initializes the SecureComputationEngine.
   * Calling this multiple times does nothing as an SecureComputationEngine can only be setup once.
   * This method is called from \code{runApplication} as well to ensure that the
   * SecureComputationEngine is setup before evaluating the application. The reason this method is
   * public is to force initialization of resources before running an
   * application. This might be needed in some cases. If you have no need for
   * this, just let the SecureComputationEngine handle it itself.
   *
   * @throws IOException If an error occurs while setting up IO related services such as network.
   */
  void setup() throws IOException;

  /**
   * Ensures that resources are shut down properly. Network is disconnected
   * and sockets are released.
   */
  void shutdownSCE();
}