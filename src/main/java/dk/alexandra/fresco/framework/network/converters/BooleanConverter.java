package dk.alexandra.fresco.framework.network.converters;

import java.nio.ByteBuffer;

public class BooleanConverter {

	
	public static byte[] toBytes(boolean b) {
		if(b) {
			return new byte[] {0x01};
		} else {
			return new byte[] {0x00};
		}
	}
	
	public static boolean fromBytes(ByteBuffer buffer) {
		byte b = buffer.get();
		if(b == 0x01) {
			return true;
		} else {
			return false;
		}
	}
}
