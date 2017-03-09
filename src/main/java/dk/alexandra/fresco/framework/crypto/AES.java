package dk.alexandra.fresco.framework.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.crypto.cipher.CryptoCipherFactory;
import org.apache.commons.crypto.cipher.CryptoCipherFactory.CipherProvider;
import org.apache.commons.crypto.stream.CryptoInputStream;
import org.apache.commons.crypto.stream.CryptoOutputStream;

public class AES {

	private final SecretKeySpec secretKey;
	private final IvParameterSpec iv;

	private Properties properties;
	private final String transform = "AES/CTR/NoPadding";

	private int maxMessageLengthInBytes;

	public AES(String secretSharedKey, int maximumMessageLengthInBytes) {
		this.maxMessageLengthInBytes = maximumMessageLengthInBytes;

		this.properties = new Properties();
		properties.setProperty(CryptoCipherFactory.CLASSES_KEY, CipherProvider.JCE.getClassName());

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			throw new RuntimeException("SHA-256 does not exist on your computers system", e1);
		}
		byte[] secretSharedKeyBytes = digest.digest(getUTF8Bytes(secretSharedKey));

		this.secretKey = new SecretKeySpec(secretSharedKeyBytes, "AES");
		this.iv = new IvParameterSpec(getUTF8Bytes("1234567890123456"));
	}

	public synchronized byte[] encrypt(byte[] data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CryptoOutputStream cos = new CryptoOutputStream(transform, properties, bos, this.secretKey, this.iv);

		cos.write(data);
		cos.flush();
		cos.close();

		return bos.toByteArray();
	}

	public synchronized byte[] decrypt(byte[] data) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		CryptoInputStream cis = new CryptoInputStream(transform, properties, bis, this.secretKey, this.iv);
		byte[] decryptedData = new byte[maxMessageLengthInBytes];
		int decryptedLen = 0;
		int i;
		while ((i = cis.read(decryptedData, decryptedLen, decryptedData.length - decryptedLen)) > -1) {
			decryptedLen += i;
		}
		cis.close();
		byte[] res = new byte[decryptedLen];
		System.arraycopy(decryptedData, 0, res, 0, decryptedLen);
		return res;
	}

	/**
	 * Converts String to UTF8 bytes
	 *
	 * @param input
	 *            the input string
	 * @return UTF8 bytes
	 */
	private static byte[] getUTF8Bytes(String input) {
		return input.getBytes(StandardCharsets.UTF_8);
	}
}
