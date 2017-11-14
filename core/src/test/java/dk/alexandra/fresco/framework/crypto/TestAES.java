package dk.alexandra.fresco.framework.crypto;

import java.io.IOException;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class TestAES {

	@Test
	public void testEncryptAndDecrypt() throws IOException {
		AES aes = new AES("89SHJsdhu3h4jbd82h", 1024, new Random());
		byte[] data = new byte[] {0x02, 0x04, 0x10, 0x77, 0x45, 0x00};
		byte[] enc = aes.encrypt(data);
		byte[] dec = aes.decrypt(enc);
		Assert.assertArrayEquals(data, dec);

		AES aes2 = new AES("89SHJsdhu3h4jbd82h", 1024, new Random());
		dec = aes2.decrypt(enc);
		Assert.assertArrayEquals(data, dec);
	}
}
