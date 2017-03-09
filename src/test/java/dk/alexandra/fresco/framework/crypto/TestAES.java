package dk.alexandra.fresco.framework.crypto;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class TestAES {

	@Test
	public void testAes() throws IOException {		
		AES aes = new AES("89SHJsdhu3h4jbd82h", 1024);
		byte[] data = new byte[] {0x02, 0x04, 0x10};
		byte[] enc = aes.encrypt(data);
		byte[] dec = aes.decrypt(enc);		
		Assert.assertArrayEquals(data, dec);
		
		
		AES aes2 = new AES("89SHJsdhu3h4jbd82h", 1024);
		dec = aes2.decrypt(enc);
		Assert.assertArrayEquals(data, dec);
	}
}
