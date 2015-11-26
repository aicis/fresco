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
package dk.alexandra.fresco.suite.bgw;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.Party;


public final class ShamirShare implements Serializable {

	private static final long serialVersionUID = -7986019375218481628L;

	private static BigInteger[] vector;

    // private static final String primeNumber = "30916444023318367583";
    // private static final String primeNumber =
    // "35742549198872617291353508656626642567";
    // public static final BigInteger primeNumber = BigInteger.valueOf(19);
 //    public static final BigInteger primeNumber = new BigInteger(
 //    "18446744073709551667");
  //   public static final BigInteger primeNumber = new BigInteger("618970019642690137449562111"); // 2^89-1, we need a Mersenne prime for the comparison protocol to be correct
  //  public static final BigInteger primeNumber = new BigInteger(
  //  "2147483647");
	
	private static BigInteger primeNumber;
	public static void setPrimeNumber(BigInteger mod) {
		primeNumber = mod;
	}

    private static SecureRandom random = new SecureRandom();
    //public static final int size = 9;
    public static final int size = 12;
    private static byte[] randomBytesBuffer;
    public static int partyId;
    private static int randomBytesMarker = 0;

    private byte point;
    private BigInteger fieldValue;
    public long p1, p2, p3, p4, p5, p6, p7;

    //private boolean ready = false; //indicates if we know the secret
    
    public ShamirShare(int point, BigInteger v) {
  //  	System.out.println("pr: "+primeNumber.bitLength());
        if (point > 255) {
            throw new IllegalArgumentException(
            "point is too large, it is more than 255.");
        }
        this.point = (byte) point;
        this.fieldValue = v.mod(primeNumber);
    }

    public ShamirShare(byte[] receivedData) {
        this.point = receivedData[0];
        int fieldSize = receivedData.length - 1;
        byte[] bytes = new byte[fieldSize];
        System.arraycopy(receivedData, 1, bytes, 0, fieldSize);
        this.fieldValue = new BigInteger(bytes);
    }

    public void setBytes(byte[] receivedData) {
        this.point = receivedData[0];
        int fieldSize = receivedData.length - 1;
        byte[] bytes = new byte[fieldSize];
        System.arraycopy(receivedData, 1, bytes, 0, fieldSize);
        this.fieldValue = new BigInteger(bytes);
    }

    public ShamirShare(BigInteger f) {
        this.fieldValue = f;
        this.point = -1;
    }

    public BigInteger getField() {
        return this.fieldValue;
    }

    public byte getPoint() {
        return this.point;
    }

    public static int getSize() {
        return ShamirShare.size + 1;
    }

    public byte[] toByteArray() {
        byte[] bytes = new byte[ShamirShare.getSize()];
        bytes[0] = this.point;
        this.copyAndInvertArray(bytes, this.fieldValue.toByteArray());
        return bytes;
    }

    private void copyAndInvertArray(byte[] bytes, byte[] byteArray) {
        for (int inx = 0; inx < byteArray.length; inx++) {
            bytes[bytes.length - byteArray.length + inx] = byteArray[inx];
        }
    }

    public BigInteger getValue() {
        return this.fieldValue;
    }

    @Override
    public String toString() {
        return "(" + this.point + ", " + this.fieldValue.toString() + ")";
    }

    public ShamirShare mult(ShamirShare other) {
        return new ShamirShare(this.point, this.fieldValue
                .multiply(other.fieldValue));
    }

    public void setPoint(int inx) {
        this.point = (byte) inx;
    }

    public static BigInteger recombine(ShamirShare[] shares,
            int numberOfParties) {
        // ShamirReporter.report(partyId, "");
        // ShamirReporter.report(partyId, " === Recombine ===");
        //
        // ShamirReporter.report(partyId, shares.toString());

        // System.out.println(Arrays.toString(shares));
        if ((vector != null) && (vector.length == numberOfParties)) {
            // System.out.println("Recombine using vector: "
            // + Arrays.toString(vector));
            return recombine(shares);
        } else {
            // System.out.println("Computing vector.");
            vector = computeCoefficients(numberOfParties);
            // System.out.println("Recombine using vector: "
            // + Arrays.toString(vector));
            return recombine(shares);
        }
    }
    
    public static BigInteger recombine(List<ShamirShare> shares,
            int numberOfParties) {
        ShamirShare[] tmp = new ShamirShare[shares.size()];
        for(int i = 0; i < shares.size(); i++) {
        	tmp[i] = shares.get(i);
        }
        return recombine(tmp, numberOfParties);
    }

    private static BigInteger recombine(ShamirShare[] shares) {
        BigInteger s = BigInteger.ZERO;
        for (int inx = 0; inx < vector.length; inx++) {
            // System.out.println("Multing: " + shares[inx] + ", " +
            // vector[inx]);
            ShamirShare share = shares[inx];
            s = s.add(share.fieldValue.multiply(vector[inx]));

            // System.out.println("Res: " + s);
        }
        // System.out.println("s = " + s.mod(primeNumber));
        return s.mod(primeNumber);
    }

    private static BigInteger[] computeCoefficients(int numberOfParties) {
        BigInteger[] vector = new BigInteger[numberOfParties];
        for (byte pi = 1; pi <= numberOfParties; pi++) {
            // String str = "";
            List<BigInteger> factors = new ArrayList<BigInteger>(
                    numberOfParties);
            for (byte pk = 1; pk <= numberOfParties; pk++) {
                if (pi != pk) {
                    BigInteger x_k = BigInteger.valueOf(pk);
                    BigInteger x_i = BigInteger.valueOf(pi);
                    BigInteger subtractionResult = x_i.subtract(x_k);
                    BigInteger subtractionResultModInverse = subtractionResult.modInverse(primeNumber);
                    // System.out.println(subtractionResult);
                    // System.out.println(subtractionResultModInverse);
                    BigInteger multiplicationResult = x_k
                    .multiply(subtractionResultModInverse);
                    BigInteger factor = multiplicationResult;
                    // str = " f_" + pi + " = " + factor + " = " + pk
                    // + "/(" + pi + "-" + pk + ")";
                    // System.out.println(str + " % " + primeNumber);
                    factors.add(factor);
                }
            }
            // System.out.println("Pi: " + pi + " factors: " + factors);

            if (factors.size() > 0) {
                // str = "";
                BigInteger r = factors.remove(0);
                for (BigInteger f : factors) {
                    // str += r + " * " + f;
                    r = r.multiply(f);
                }

                // System.out.println("r: " + r + " = " + str
                // + " % "
                // + primeNumber);
                vector[pi - 1] = r;
            }
        }
        return vector;
    }

    
    public byte getType() {
        return 1;
    }

    
    public byte[] getPayload() {
        return this.toByteArray();
    }

    public static ShamirShare[] createShares(BigInteger secret,
            Party[] parties, int threshold) {
        return ShamirShare.createShares(secret, parties.length, threshold);
    }

    public static ShamirShare[] createShares(BigInteger secret,
            int numberOfParties, int threshold) {
        // ShamirReporter.report(this.partyId, "I am sending.");
        List<BigInteger> coefficients = new ArrayList<BigInteger>(threshold);
        coefficients.add(secret);

        for (int inx = 0; inx < threshold; inx++) {
            coefficients.add(ShamirShare.random());
        }

        ShamirShare[] shares = new ShamirShare[numberOfParties];

        // ShamirReporter.report(this.partyId, "coeff: " +
        // coefficients.toString());
        for (int inx = 1; inx <= numberOfParties; inx++) {
            // Instead of calculating s_i as
            // s_i = s + a_1 x_i + a_2 x_i^2 + ... + a_t x_i^t
            //
            // we avoid the exponentiations by calculating s_i by
            //
            // s_i = s + x_i (a_1 + x_i (a_2 + x_i ( ... (a_t) ...
            // )))
            //
            // This is a little faster, even for small n and t.
            BigInteger cur_point = BigInteger.valueOf(inx);
            BigInteger cur_share = coefficients.get(threshold);
            // Go backwards from this.threshold-1 down to 0
            for (int inj = threshold - 1; inj >= 0; inj--) {

                cur_share = coefficients.get(inj).add(
                        cur_share.multiply(cur_point));
            }
            shares[inx - 1] = new ShamirShare(inx, cur_share);
        }
        return shares;
    }
    
    
    public void setField(BigInteger field) {
        this.fieldValue = field;
    }

    public static BigInteger random() {
        byte[] bytes = new byte[8];
        // ShamirReporter.report(partyId, "Marker: " + randomBytesMarker);
        // if (randomBytesBuffer != null) {
        // ShamirReporter.report(partyId, "D: "
        // + randomBytesBuffer.length);
        // }
        if ((randomBytesBuffer != null)
                && (randomBytesMarker + 8 < randomBytesBuffer.length)) {
            // ShamirReporter.report(partyId, "Reuse");
            System.arraycopy(randomBytesBuffer, randomBytesMarker, bytes, 0, 8);
            randomBytesMarker += 8;
            return new BigInteger(bytes);
        } else {
            // ShamirReporter.report(partyId, "New");
            randomBytesBuffer = random0(16384);
            randomBytesMarker = 0;
            System.arraycopy(randomBytesBuffer, randomBytesMarker, bytes, 0, 8);
            randomBytesMarker += 8;
        }
        return new BigInteger(bytes);
        // byte bytes[] = new byte[8];
        // random.nextBytes(bytes);
        // return new GaloisField(new BigInteger(bytes));
    }

    private static byte[] random0(int numberOfBytes) {
        byte bytes[] = new byte[numberOfBytes];
        random.nextBytes(bytes);
        return bytes;
    }

    public static void setRandomSeed(byte[] seed) {
        random = new SecureRandom(seed);
    }

    /**
     * Deserialises a byte array into a shamir share array.
     * NOT TESTED YET 
     * @param bytes Array of bytes
     * @return Array of shamir shares
     */
    public static ShamirShare[] deSerializeArray(byte[] bytes, int count){
    	ShamirShare[] res = new ShamirShare[count];
    	int counter = 0;
    	int indx = 0;
    	byte[] b = null;
    	while (indx<bytes.length){
    		byte l = bytes[indx];
    		b = new byte[l];
    		System.arraycopy(bytes, indx, b, 0, l);
    		res[counter] = new ShamirShare(b);
    		counter++;
    		indx = indx + l;
    	}
    	return res;
    }
    
    public static ShamirShare deSerialize(byte[] bytes, int offset){
    	byte[] b = new byte[getSize()];
    	System.arraycopy(bytes, offset, b, 0, getSize());
    	return new ShamirShare(b);
    }
    //stub
    public boolean isReady(){
    	return false;
    }
}
