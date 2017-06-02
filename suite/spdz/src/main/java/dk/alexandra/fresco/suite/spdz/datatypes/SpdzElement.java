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

import dk.alexandra.fresco.suite.spdz.utils.Util;
import java.io.Serializable;
import java.math.BigInteger;

public class SpdzElement implements Serializable{
	
	private static final long serialVersionUID = 6794633112697012286L;
	private BigInteger share;
	private BigInteger mac;
	private final BigInteger mod = Util.getModulus();
	
	public SpdzElement(){
		this.share = null;
		this.mac = null;
	}
		
	public SpdzElement(BigInteger share, BigInteger mac){
		this.share = share;
		this.mac = mac;		
	}
	
	//Communication methods	
	public SpdzElement(byte[] data){
		int size = getSize();
		byte[] shareBytes = new byte[size];
		byte[] macBytes = new byte[size];
		for(int i = 0; i < data.length/2; i++){
			shareBytes[i] = data[i];
			macBytes[i] = data[size+i];
		}
		this.share = new BigInteger(shareBytes);
		this.mac = new BigInteger(macBytes);
	}

	public static int getSize(){
		return Util.getModulusSize(); //cause we only do partial openings meaning sending a share
	}
	
	public byte[] toByteArray(){		
		byte[] share_invert = new byte[getSize()];
		byte[] mac_invert = new byte[getSize()];
		copyAndInvertArray(share_invert, this.share.toByteArray());
		copyAndInvertArray(mac_invert, this.mac.toByteArray());
		byte[] res = new byte[getSize()*2];
		System.arraycopy(share_invert, 0, res, 0, getSize());
		System.arraycopy(mac_invert, 0, res, getSize(), getSize());
		return res;		
	}
	
	private void copyAndInvertArray(byte[] bytes, byte[] byteArray) {
        for (int inx = 0; inx < byteArray.length; inx++) {       
            bytes[bytes.length - byteArray.length + inx] = byteArray[inx];            
        }
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
		return new SpdzElement(rShare, rMac);
	}
	
	/**
	 * Public value added
	 * @param e
	 * @param myId
	 * @return
	 */
	public SpdzElement add(SpdzElement e, int pID){
		BigInteger rShare = this.share;
		BigInteger rMac = this.mac;
		rMac = rMac.add(e.getMac()).mod(mod);
		if(pID == 1){
			rShare = rShare.add(e.getShare()).mod(mod);			
		}
		return new SpdzElement(rShare, rMac);
	}
	
	public SpdzElement subtract(SpdzElement e){
		BigInteger eShare = e.getShare();
		BigInteger rShare = this.share.subtract(eShare).mod(mod);
		BigInteger eMac = e.getMac();
		BigInteger rMac = this.mac.subtract(eMac).mod(mod);				
		return new SpdzElement(rShare, rMac);
	}
	
	/**
	 * Public value subtracted
	 * @param e
	 * @param myId
	 * @return
	 */
	public SpdzElement subtract(SpdzElement e, int pID) {
		BigInteger rShare = this.share;
		if(pID == 1){
			rShare = this.share.subtract(e.getShare()).mod(mod);
		}
		BigInteger eMac = e.getMac();
		BigInteger rMac = this.mac.subtract(eMac).mod(mod);				
		return new SpdzElement(rShare, rMac);
	}
	
	public SpdzElement multiply(BigInteger c){
		BigInteger rShare = this.share.multiply(c).mod(mod);
		BigInteger rMac = this.mac.multiply(c).mod(mod);				
		return new SpdzElement(rShare, rMac);		
	}
	
	
	//Utility methods
	@Override
	public String toString(){
		return "spdz("+share+", "+mac+")";
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof SpdzElement){
			SpdzElement e = (SpdzElement)o;		
			return (e.getShare().equals(share) && e.getMac().equals(mac));
		}else{
			return false;
		}
	}
}
