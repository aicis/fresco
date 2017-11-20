package dk.alexandra.fresco.suite.spdz.datatypes;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Random;

public class SpdzCommitment {

	private BigInteger value;
	private BigInteger randomness;
	private Random rand;
	private MessageDigest H;

	public SpdzCommitment(MessageDigest H, BigInteger value, Random rand){
		this.value = value;
		this.rand = rand;
		this.H = H;
	}

	public BigInteger getCommitment(BigInteger modulus) {
		H.update(value.toByteArray());
		this.randomness = new BigInteger(modulus.bitLength(), rand);
		H.update(this.randomness.toByteArray());
		return new BigInteger(H.digest()).mod(modulus);
	}

	public BigInteger getValue(){
		return this.value;
	}

	public BigInteger getRandomness(){
		return this.randomness;
	}

	@Override
	public String toString(){
		return "SpdzCommitment[v:"+this.value+", r:"+this.randomness+"]";
	}
}
