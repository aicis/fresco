package dk.alexandra.fresco.framework.network.serializers;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Test;

import dk.alexandra.fresco.suite.spdz.utils.Util;

import org.junit.Assert;

/**
 * Tests that the Serializers works as expected.
 * 
 * @author Kasper Damgaard
 *
 */
public class TestSerializers {

	@Test
	public void testBigIntegerSerializer() {
		BigInteger b = new BigInteger("12983762173218321342");
		byte[] bytes = BigIntegerSerializer.toBytes(b);
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		BigInteger bb = BigIntegerSerializer.toBigInteger(buf);
		Assert.assertEquals(b, bb);
	}

	@Test
	public void testBigIntegerWithFixedLengthSerializer() {
		BigInteger mod = new BigInteger("1298376217321832134223");
		Util.setModulus(mod);

		BigInteger b = new BigInteger("1298376217321832");
		byte[] bytes = BigIntegerWithFixedLengthSerializer.toBytes(b, Util.getModulusSize());
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		BigInteger bb = BigIntegerWithFixedLengthSerializer.toBigInteger(buf, Util.getModulusSize());
		Assert.assertEquals(b, bb);

		b = BigInteger.ZERO;
		bytes = BigIntegerWithFixedLengthSerializer.toBytes(b, Util.getModulusSize());
		buf = ByteBuffer.wrap(bytes);
		bb = BigIntegerWithFixedLengthSerializer.toBigInteger(buf, Util.getModulusSize());
		Assert.assertEquals(b, bb);
	}

	@Test
	public void testByteArrayHelper() {
		BigInteger b = new BigInteger("12983762173218321342");
		byte[] bytes = b.toByteArray();
		byte[] res = ByteArrayHelper.addSize(bytes);
		byte[] bb = ByteArrayHelper.getByteObject(ByteBuffer.wrap(res));
		Assert.assertArrayEquals(bytes, bb);
	}

	@Test
	public void testBooleanSerializer() {
		boolean b = true;
		byte[] bytes = BooleanSerializer.toBytes(b);
		boolean bb = BooleanSerializer.fromBytes(ByteBuffer.wrap(bytes));
		Assert.assertEquals(b, bb);

		b = false;
		bytes = BooleanSerializer.toBytes(b);
		bb = BooleanSerializer.fromBytes(ByteBuffer.wrap(bytes));
		Assert.assertEquals(b, bb);
	}
}
