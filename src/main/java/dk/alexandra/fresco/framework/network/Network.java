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

import java.io.IOException;
import java.io.Serializable;

/**
 * A player's view of a network. 
 * 
 */
public interface Network {

	/**
	 * Attempts to connect with other players.
	 * 
	 * Blocks until connection is successful.
	 * 
	 * @param timeoutMillis
	 *            The amount of milliseconds before an IOException is thrown in
	 *            case of connection failure.
	 */
	void connect(int timeoutMillis) throws IOException;

	/**
	 * Send data to other party with id partyId.
	 * 
	 * @param channel
	 *            the channel to send data over.
	 * @param partyId
	 *            the party to send data to
	 * @param data
	 *            the data to send
	 * @throws IOException
	 *             thrown if the connection has problems.
	 */
	void send(String channel, int partyId, Serializable data)
			throws IOException;

	/**
	 * Blocking call that only returns once the data has been fully received and
	 * deserialized.
	 * 
	 * @param channel
	 *            the channel to receive from
	 * @param partyId
	 *            the party to receive from
	 * @return the data send by the given partyId through the given channel
	 * @throws IOException
	 *             if the channel times out or other connection issues occurs.
	 */
	<T extends Serializable> T receive(String channel, int partyId) throws IOException;

	public void close() throws IOException;
}
