/*******************************************************************************
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
package dk.alexandra.fresco.lib.helper.sequential;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.lib.helper.AppendableProtocolProducer;

public class SequentialProtocolProducer implements ProtocolProducer, ProtocolProducerList, AppendableProtocolProducer {

	private SequentialHelper seqh;
	
	protected LinkedList<ProtocolProducer> cs = new LinkedList<ProtocolProducer>();

	public SequentialProtocolProducer(ProtocolProducer... cs) {
		seqh = new SequentialHelper(this);
		for (ProtocolProducer c : cs) {
			this.cs.add(c);
		}
	}
	
	public void append(ProtocolProducer c) {
		this.cs.add(c);
	}

	@Override
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos) {
		return seqh.getNextProtocols(nativeProtocols, pos);
	}

	@Override
	public boolean hasNextProtocols() {
		return seqh.hasNextProtocols();
	}

	@Override
	public ProtocolProducer getNextInLine() {
		return cs.pop();
	}

	@Override
	public boolean hasNextInLine() {
		return !(cs.isEmpty());
	}	
	
	public LinkedList<ProtocolProducer> merge() {
		ListIterator<ProtocolProducer> x = cs.listIterator();
		while (x.hasNext()) {
			ProtocolProducer gp = x.next();
			if (gp instanceof SequentialProtocolProducer) {
				x.remove();
				SequentialProtocolProducer seq = (SequentialProtocolProducer)gp;
				for (ProtocolProducer p: seq.cs) {
					x.add(p);
				}
			}
		}
		return cs;
	}
	
	public List<ProtocolProducer> getNextProtocolProducerLevel(){
		return cs;
	}
}
