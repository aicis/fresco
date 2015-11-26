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
package dk.alexandra.fresco.suite.spdz.datatypes;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * An inputmask for player_i is random value r 
 * shared among parties so that only player_i knows the real value r.
 */
public class SpdzInputMask implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 506302228523568567L;
	private SpdzElement mask; 
	BigInteger realValue;
	
	public SpdzInputMask(SpdzElement mask, BigInteger realValue){
		this.mask = mask;
		this.realValue = realValue;
	}
	
	public SpdzInputMask(SpdzElement mask){
		this.mask = mask;
		this.realValue = null;
	}
	
	public SpdzElement getMask(){
		return mask;
	}
	
	/**
	 * @return For the player that owns this inputmask, the 
	 * shared real value of the mask. Otherwise null.
	 */
	public BigInteger getRealValue(){
		return realValue;
	}
}
