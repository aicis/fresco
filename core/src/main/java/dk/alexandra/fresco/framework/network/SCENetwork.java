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
package dk.alexandra.fresco.framework.network;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Network towards the protocols. This does not expose the real network, and
 * sending has no effect on the TCP layer. A higher level should handle the
 * input/output (typically the evaluator)
 * 
 * @author Kasper Damgaard
 *
 */
public interface SCENetwork {

	/**
	 * Retrieves input from the given id
	 * 
	 * @param id
	 *            The id of the player you want input from. Id's start from 1.
	 * @return A byte buffer which the protocol suite can use to fetch the
	 *         wanted bytes from, or use the different converters to extract
	 *         certain data classes.
	 */
	public ByteBuffer receive(int id);

	/**
	 * Retrieves input from all players (including yourself)
	 * 
	 * @return A list of byte buffers where the data from party 1 resides at
	 *         index 0 and so forth.
	 */
	public List<ByteBuffer> receiveFromAll();

	/**
	 * Queues up a value to be send towards the given id. Values are not send by
	 * TCP by calling this method, but queued up for the higher layer to send
	 * later.
	 * 
	 * @param id
	 *            The id whom you want to send to. Id's start from 1.
	 * @param data
	 *            The value to send
	 */
	public void send(int id, byte[] data);

	/**
	 * Queues up a value to be send to all parties (yourself included). Values
	 * are not send by TCP by calling this method, but queued up for the higher
	 * layer to send later.
	 * 
	 * @param data
	 *            The value to send to all parties
	 */
	public void sendToAll(byte[] data);

	/**
	 * Queues up different values to be send to all parties (yourself included).
	 * Values are not send by TCP by calling this method, but queued up for the
	 * higher layer to send later.
	 * 
	 * @param data
	 *            The data to send to the parties.
	 */
	public void sendSharesToAll(byte[][] data);

	/**
	 * Let's the network strategy know that you expect input from the given id
	 * in the next round.
	 * 
	 * @param id
	 *            The id to expect input from in the next round. Id's start from
	 *            1.
	 */
	public void expectInputFromPlayer(int id);

	/**
	 * Let's the network strategy know that you expect to receive input from
	 * everyone next round.
	 */
	public void expectInputFromAll();

	// TODO: Remove from here when possible. Requires solution to preprocessed
	// data.
	/**
	 * Returns the threadId that this protocol network is part of.
	 * 
	 * @return
	 */
	public int getThreadId();

}
