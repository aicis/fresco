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
package dk.alexandra.fresco.framework.sce.configuration;

import java.util.Map;
import java.util.logging.Level;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;

public interface SCEConfiguration {

	public int getMyId();

	public Map<Integer, Party> getParties();

	/**
	 * Defaults to info if logLevel is not found in the properties file.
	 * 
	 * @return
	 */
	public Level getLogLevel();

	/**
	 * Returns the name of the protocol suite that should run the MPC
	 * computations.
	 * 
	 * @return
	 */
	public String getProtocolSuiteName();

	/**
	 * Returns -1 if no such property is found.
	 * 
	 * @return
	 */
	public int getNoOfThreads();

	/**
	 * Reads the config for an indication on which kind of GateEvaluator should
	 * be used.
	 * 
	 * @return
	 */
	public ProtocolEvaluator getEvaluator();

	/**
	 * Returns -1 if no such property is found.
	 * 
	 * @return
	 */
	public int getNoOfVMThreads();

	/**
	 * Returns the storage requested.
	 * 
	 * @return
	 */
	public Storage getStorage();

	/**
	 * Returns the streamed storage requested.
	 * 
	 * @return
	 */
	public StreamedStorage getStreamedStorage();

	/**
	 * Returns the maximum batch size that the evaluators should run with.
	 * 
	 * @return
	 */
	public int getMaxBatchSize();
}
