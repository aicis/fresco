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
package dk.alexandra.fresco.framework.sce.resources;

import java.security.SecureRandom;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.framework.sce.resources.threads.ProtocolThreadPool;

public interface ResourcePool {

	/**
	 * Returns the id of the party
	 * 
	 * @return
	 */
	public abstract int getMyId();

	/**
	 * Returns the number of players.
	 * 
	 * @return
	 */
	public abstract int getNoOfParties();

	/**
	 * Returns the threadpool a protocol suite can use for doing multithreaded
	 * work that is not controlled by the SCE. We advise that protocol suite
	 * developers use this since the control over how many threads are in use is
	 * then delegated to the SCE.
	 * 
	 * @return
	 */
	public abstract ProtocolThreadPool getThreadPool();

	/**
	 * Returns the number of threads that the SCE will maximally use when
	 * evaluating.
	 * 
	 * @return
	 */
	public abstract int getVMThreadCount();

	/**
	 * Returns the raw network in case the protocol suite needs access to this.
	 * It should not be used for the individual protocols, but rather only for
	 * doing some work before or after an application evaluation.
	 * 
	 * @return
	 */
	public abstract Network getNetwork();

	/**
	 * Returns the storage native to the system. Use this for persistent data
	 * where random access is preferred.
	 * 
	 * @return
	 */
	public abstract Storage getStorage();

	/**
	 * Returns the streamed storage native to the system. Use this for
	 * persistent data where random access is not preferred.
	 * 
	 * @return
	 */
	public abstract StreamedStorage getStreamedStorage();

	/**
	 * Returns the randomness generator of the system. Use this for getting
	 * random data that does not need to be cryptographically secure.
	 * 
	 * @return
	 */
	public abstract Random getRandom();

	/**
	 * Returns the secure version of the randomness generator of the system. Use
	 * where the randomness needs to be crypographically secure.
	 * 
	 * @return
	 */
	public abstract SecureRandom getSecureRandom();

}