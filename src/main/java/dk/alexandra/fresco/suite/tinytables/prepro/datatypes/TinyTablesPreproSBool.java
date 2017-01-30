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
package dk.alexandra.fresco.suite.tinytables.prepro.datatypes;

import dk.alexandra.fresco.framework.util.ot.Encoding;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;

/**
 * This class represents a masked boolean value in the preprocessing phase of
 * the TinyTables protocol suite. Note that in the preprocessing phase, no
 * values are assigned to the wires, so this class only handles the players
 * share of the masking parameter of the wire.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesPreproSBool implements SBool {

	private static final long serialVersionUID = 8582913017231020209L;
	private TinyTablesElement value; // Additive share of mask of this SBool
	private boolean ready;

	public TinyTablesPreproSBool(TinyTablesElement share) {
		this.setValue(share);
	}

	public TinyTablesPreproSBool() {
		// Not ready yet
	}

	@Override
	public byte[] getSerializableContent() {
		return new byte[] { Encoding.encodeBoolean(value.getShare()) };
	}

	@Override
	public void setSerializableContent(byte[] val) {
		setValue(new TinyTablesElement(Encoding.decodeBoolean(val[0])));
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	/**
	 * Get this players share of the mask <i>r</i> of the wire this SBool
	 * corresponds to.
	 * 
	 * @return
	 */
	public TinyTablesElement getValue() {
		return value;
	}

	/**
	 * Set this players share of the mask <i>r</i> of the wire this SBool
	 * corresponds to.
	 * 
	 * @param share
	 */
	public void setValue(TinyTablesElement share) {
		this.value = share;
		this.ready = true;
	}

	@Override
	public String toString() {
		return "TinyTablePreproSBool [share=" + value + "]";
	}

}
