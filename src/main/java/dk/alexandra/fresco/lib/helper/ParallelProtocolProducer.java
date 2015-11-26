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
 * If a ParallelCircuit has n subcircuits and is asked to deliver m gates, it
 * requests m/n gates from each of the subcircuits.
 * 
 */
public class ParallelProtocolProducer implements ProtocolProducer,
		AppendableProtocolProducer {

	private LinkedList<ProtocolProducer> cs;
	private ListIterator<ProtocolProducer> iterator;

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
		// TODO: Should we prevent appending a new circuit after first call to
		// getNextGate?
		cs.offer(c);
	}

	@Override
	public boolean hasNextProtocols() {
		prune();
		return !cs.isEmpty();
	}

	/**
	 * Removes any empty circuits.
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
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (pos < 0 || pos >= gates.length) {
			throw new MPCException(
					"Index out of bounds, gates.length=" + gates.length
							+ ", pos=" + pos);
		}
		// TODO: This is a simple, but very rough implementation.
		// It requests an equal amount from each subcircuit and only asks once.
		// A better implementation should try to fill up the gate array by
		// requesting further gates from large circuits if the smaller circuits
		// run dry.
		// E.g. this implementation is inferior in that it may return less gates
		// than it could.
		if (cs.size() == 0) {
			return pos;
		}
		ListIterator<ProtocolProducer> x = cs.listIterator();
		while (x.hasNext()) {
			ProtocolProducer c = x.next();
			pos = c.getNextProtocols(gates, pos);
			if (!c.hasNextProtocols()) {
				x.remove();
			}
			if (pos == gates.length) {
				break; // We've filled the array.
			}
		}
		return pos;
	}

	public LinkedList<ProtocolProducer> merge() {
		ListIterator<ProtocolProducer> x = cs.listIterator();
		LinkedList<ProtocolProducer> merged = new LinkedList<ProtocolProducer>();
		while (x.hasNext()) {
			ProtocolProducer gp = x.next();
			if (gp instanceof ParallelProtocolProducer) {
				x.remove();
				ParallelProtocolProducer par = (ParallelProtocolProducer) gp;
				for (ProtocolProducer p : par.cs) {
					x.add(p);
				}
			}
		}
		merged.addAll(cs);
		return merged;
	}

	public List<ProtocolProducer> getNextGateProducerLevel() {
		return cs;
	}
}
