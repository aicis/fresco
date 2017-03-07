package dk.alexandra.fresco.framework.network.converters;

import java.nio.ByteBuffer;

public class IntegerConverter {

	
	public static byte[] toBytes(int i) {
		return ByteBuffer.allocate(4).putInt(i).array();
	}
	
	public static int fromBytes(ByteBuffer buffer) {
		return buffer.getInt();
	}
}
