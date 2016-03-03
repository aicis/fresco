/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.ninja.prepro;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.OpenBoolProtocol;
import dk.alexandra.fresco.suite.ninja.NinjaOBool;
import dk.alexandra.fresco.suite.ninja.NinjaProtocolSuite;
import dk.alexandra.fresco.suite.ninja.protocols.NinjaProtocol;
import dk.alexandra.fresco.suite.ninja.storage.NinjaStorage;
import dk.alexandra.fresco.suite.ninja.storage.PrecomputedOutputNinja;

public class DummyPreproOpenToAllProtocol extends NinjaProtocol implements OpenBoolProtocol{

	private PreproNinjaSBool toOpen;
	private NinjaOBool opened;
	
	public DummyPreproOpenToAllProtocol(int id, PreproNinjaSBool toOpen, NinjaOBool opened) {
		super();
		this.id = id;
		this.toOpen = toOpen;
		this.opened = opened;

	}

	@Override
	public Value[] getInputValues() {
		return new Value[] {toOpen};
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] {opened};
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {		
		NinjaStorage storage1 = NinjaProtocolSuite.getInstance(1).getStorage();
		PrecomputedOutputNinja ninja = new PrecomputedOutputNinja(new byte[] {0, 1});
		storage1.storeOutputNinja(id, ninja);
		
		NinjaStorage storage2 = NinjaProtocolSuite.getInstance(2).getStorage();
		storage2.storeOutputNinja(id, ninja);
		
		return EvaluationStatus.IS_DONE;
	}

}
