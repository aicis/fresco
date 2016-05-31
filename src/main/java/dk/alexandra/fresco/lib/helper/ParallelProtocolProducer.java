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
package dk.alexandra.fresco.lib.helper;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;

/**
 * If a Parallelprotocol has n subprotocols and is asked to deliver m protocols, it
 * requests m/n protocols from each of the subprotocols.
 * 
 */
public class ParallelProtocolProducer implements ProtocolProducer,
		AppendableProtocolProducer {

	private LinkedList<ProtocolProducer> cs;

	public ParallelProtocolProducer() {
		cs = new LinkedList<ProtocolProducer>();
	}

	public ParallelProtocolProducer(ProtocolProducer... cs) {
		this();
		for (ProtocolProducer c : cs) {
			append(c);
		}
	}

	public List<ProtocolProducer> getProducers() {
		// this.merge();
		return cs;
	}

	public void append(ProtocolProducer c) {
		cs.offer(c);
	}

	@Override
	public boolean hasNextProtocols() {
		prune();
		return !cs.isEmpty();
	}

	/**
	 * Removes any empty protocols.
	 * 
	 */
	private void prune() {
		while (!cs.isEmpty()) {
			if (cs.getFirst().hasNextProtocols()) {
				return;
			} else {
				cs.remove();
			}
		}
	}

	@Override
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos) {
		if (pos < 0 || pos >= nativeProtocols.length) {
			throw new MPCException(
					"Index out of bounds, length=" + nativeProtocols.length
							+ ", pos=" + pos);
		}
		// TODO: This is a simple, but very rough implementation.
		// It requests an equal amount from each subprotocol and only asks once.
		// A better implementation should try to fill up the protocol array by
		// requesting further protocols from large protocols if the smaller protocols
		// run dry.
		// E.g. this implementation is inferior in that it may return less protocols
		// than it could.
		if (cs.size() == 0) {
			return pos;
		}
		ListIterator<ProtocolProducer> x = cs.listIterator();
		while (x.hasNext()) {
			ProtocolProducer c = x.next();
			pos = c.getNextProtocols(nativeProtocols, pos);
			if (!c.hasNextProtocols()) {
				x.remove();
			}
			if (pos == nativeProtocols.length) {
				break; // We've filled the array.
			}
		}
		return pos;
	}

	public LinkedList<ProtocolProducer> merge() {
		ListIterator<ProtocolProducer> x = cs.listIterator();
		LinkedList<ProtocolProducer> merged = new LinkedList<ProtocolProducer>();
		while (x.hasNext()) {
			ProtocolProducer pp = x.next();
			if (pp instanceof ParallelProtocolProducer) {
				x.remove();
				ParallelProtocolProducer par = (ParallelProtocolProducer) pp;
				for (ProtocolProducer p : par.cs) {
					x.add(p);
				}
			}
		}
		merged.addAll(cs);
		return merged;
	}

	public List<ProtocolProducer> getNextProtocolProducerLevel() {
		return cs;
	}
}
