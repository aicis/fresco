package dk.alexandra.fresco.framework.network.converters;

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
	
	public static BigInteger toBigInteger(ByteBuffer buffer) {
		byte[] content = new byte[buffer.get()];
		buffer.get(content);
		return new BigInteger(content);
	}
}
