/*
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

public class SpdzElement implements Serializable{

  private static final long serialVersionUID = 8828769687281856043L;
  private final BigInteger share;
  private final BigInteger mac;
  private final BigInteger mod;

  // For serialization
  public SpdzElement(){
    this.share = null;
    this.mac = null;
    this.mod = null;
  }

  public SpdzElement(BigInteger share, BigInteger mac, BigInteger modulus) {
    this.share = share;
    this.mac = mac;
    this.mod = modulus;
  }

  //Communication methods
  public SpdzElement(byte[] data, BigInteger modulus, int modulusSize) {
    int size = modulusSize;
    byte[] shareBytes = new byte[size];
		byte[] macBytes = new byte[size];
		for(int i = 0; i < data.length/2; i++){
			shareBytes[i] = data[i];
			macBytes[i] = data[size+i];
		}
		this.share = new BigInteger(shareBytes);
		this.mac = new BigInteger(macBytes);
    mod = modulus;
  }

	//get operations
	public BigInteger getShare(){
		return share;
  }

	public BigInteger getMac(){
		return mac;
	}

	//Arithmetic operations:
	public SpdzElement add(SpdzElement e){
		BigInteger rShare = this.share.add(e.getShare()).mod(mod);
    BigInteger rMac = this.mac.add(e.getMac()).mod(mod);
    return new SpdzElement(rShare, rMac, this.mod);
  }

	/**
	 * Public value added
	 * @param e
   * @param pID
   * @return
	 */
	public SpdzElement add(SpdzElement e, int pID){
		BigInteger rShare = this.share;
		BigInteger rMac = this.mac;
		rMac = rMac.add(e.getMac()).mod(mod);
		if(pID == 1){
      rShare = rShare.add(e.getShare()).mod(mod);
    }
    return new SpdzElement(rShare, rMac, this.mod);
  }

  public SpdzElement subtract(SpdzElement e){
		BigInteger eShare = e.getShare();
		BigInteger rShare = this.share.subtract(eShare).mod(mod);
		BigInteger eMac = e.getMac();
    BigInteger rMac = this.mac.subtract(eMac).mod(mod);
    return new SpdzElement(rShare, rMac, this.mod);
  }

  public SpdzElement multiply(BigInteger c) {
    BigInteger rShare = this.share.multiply(c).mod(mod);
    BigInteger rMac = this.mac.multiply(c).mod(mod);
    return new SpdzElement(rShare, rMac, this.mod);
  }


  //Utility methods
  @Override
	public String toString(){
		return "spdz("+share+", "+mac+")";
	}

  @Override
	public boolean equals(Object o){
		if(o instanceof SpdzElement){
      SpdzElement e = (SpdzElement) o;
      return (e.getShare().equals(share) && e.getMac().equals(mac));
		}else{
			return false;
		}
	}
}
