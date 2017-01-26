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
package dk.alexandra.fresco.suite.tinytables.online.datatypes;

import dk.alexandra.fresco.framework.util.ot.Encoding;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;

/**
 * This class represents a masked boolean value in the online phase of the
 * TinyTables protocol. The two players both know the masked value, <i>e = r +
 * b</i>, but each player only knows his share of the value <i>e</i> (and of the
 * mask <i>r</i>, which was picked during the preprocessing phase).
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesSBool implements SBool {

	private static final long serialVersionUID = 8582913017231020209L;
	private TinyTablesElement value;
	
	public TinyTablesSBool() {
	}
	
	public TinyTablesSBool(TinyTablesElement share) {
		 this.value = share;
	}

	@Override
	public boolean isReady() {
		return (this.value != null);
	}

	public TinyTablesElement getValue() {
		return value;
	}

	public void setValue(TinyTablesElement share) {
		this.value = share;
	}

	@Override
	public String toString() {
		return "TinyTablesSBool [value=" + value + "]";
	}

	@Override
	public byte[] getSerializableContent() {
		return new byte[] { Encoding.encodeBoolean(value.getShare()) };
	}

	@Override
	public void setSerializableContent(byte[] val) {
		boolean share = Encoding.decodeBoolean(val[0]);
		this.setValue(new TinyTablesElement(share));
	}
	
}
