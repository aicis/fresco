package dk.alexandra.fresco.framework.network.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class BigIntegerConverter {

	public static byte[] toBytes(BigInteger b) {
		byte[] bytes = b.toByteArray();
		byte[] res = new byte[bytes.length+1];
		res[0] = (byte)bytes.length;
		System.arraycopy(bytes, 0, res, 1, bytes.length);
		return res;
	}
	
	public static byte[] toBytes(BigInteger[] bs) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for(BigInteger b : bs) {
			try {
				bos.write(toBytes(b));
			} catch (IOException e) {
				throw new RuntimeException("IOException occured while trying to convert a BigInteger to bytes", e);
			}
		}
		return bos.toByteArray();
	}
	
	public static BigInteger toBigInteger(ByteBuffer buffer) {
		byte[] content = new byte[buffer.get()];
		buffer.get(content);
		return new BigInteger(content);
	}
	
	public static BigInteger[] toBigIntegers(ByteBuffer buffer, int amount) {
		BigInteger[] res = new BigInteger[amount];
		for(int i = 0; i < amount; i++) {
			res[i] = toBigInteger(buffer);
		}
		return res;
	}
}
