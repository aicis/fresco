package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.otextension.PseudoOtp;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.bouncycastle.pqc.math.linearalgebra.BigEndianConversions.I2OSP;

/**
 * Uses Chou-Orlandi with fixes as seen in https://eprint.iacr.org/2021/1218
 *
 */
public abstract class AbstractChouOrlandiOT<T extends InterfaceOtElement<T>> implements Ot {

    private final int otherId;
    private final Network network;
    protected final Drng randNum;

    private final Mac mac;


    /**
     * Decodes an encoded element
     *
     * @param bytes the encoded element represented in bytes
     * @return the decoded element
     */
    abstract T decodeElement(byte[] bytes);

    abstract T getGenerator();

    abstract BigInteger getDhModulus();

    /**
     * multiplies the BigInteger with the Generator
     * @param input
     * @return a new Element T
     */
    abstract T multiplyWithGenerator(BigInteger input);

    public AbstractChouOrlandiOT(int otherId, Drbg randBit, Network network) {
        this.otherId = otherId;
        this.network = network;
        this.randNum = new DrngImpl(randBit);
        this.mac = new HMac(new SHA3Digest());
    }

    @Override
    public void send(StrictBitVector messageZero, StrictBitVector messageOne) {
        int maxBitLength = Math.max(messageZero.getSize(), messageOne.getSize());
        Pair<byte[], byte[]> seedMessages = sendRandomOt();
        byte[] encryptedZeroMessage = PseudoOtp.encrypt(messageZero.toByteArray(),
                seedMessages.getFirst(), maxBitLength / Byte.SIZE);
        byte[] encryptedOneMessage = PseudoOtp.encrypt(messageOne.toByteArray(),
                seedMessages.getSecond(), maxBitLength / Byte.SIZE);
        network.send(otherId, encryptedZeroMessage);
        network.send(otherId, encryptedOneMessage);
    }

    @Override
    public StrictBitVector receive(boolean choiceBit) {
        byte[] seed = receiveRandomOt(choiceBit);
        byte[] encryptedZeroMessage = network.receive(otherId);
        byte[] encryptedOneMessage = network.receive(otherId);

        if (encryptedZeroMessage.length != encryptedOneMessage.length) {
            throw new MaliciousException("The length of the two choice messages is not equal");
        }
        byte[] unpaddedMessage;
        if (choiceBit == false) {
            unpaddedMessage = PseudoOtp.decrypt(encryptedZeroMessage, seed);
        } else {
            unpaddedMessage = PseudoOtp.decrypt(encryptedOneMessage, seed);
        }
        return new StrictBitVector(unpaddedMessage);
    }

    /**
     * Completes the receiver's part of the Chou-Orlandi OT in order to receive a random message of the
     * length of hash digest.
     *
     * @return The random message received
     */
    private byte[] receiveRandomOt(boolean choiceBit) {
        // A
        T A = this.decodeElement(network.receive(otherId));
        // U = A^b*g^x
        // if b = 0 -> U = g^x
        // if b = 1 -> U = A*g^x
        BigInteger x = randNum.nextBigInteger(getDhModulus());
        T U;
        if (choiceBit == false) {
            U = A.exponentiation(BigInteger.ZERO).groupOp(getGenerator().exponentiation(x));
        } else {
            U = A.exponentiation(BigInteger.ONE).groupOp(getGenerator().exponentiation(x));
        }
        network.send(otherId, U.toByteArray());

        // k = H(A, A^x)
        byte[] key = new byte[mac.getMacSize()];
        mac.update(A.toByteArray(), 0, A.toByteArray().length);
        byte[] Ax = A.exponentiation(x).toByteArray();
        mac.update(Ax, 0, Ax.length);
        mac.doFinal(key, 0);
        mac.reset();
        return key;
    }

    /**
     * Completes the sender's part of the Chou-Orlandi OT in order to send two random messages of the
     * length of hash digest.
     *
     * @return The two random messages sent by the sender.
     */
    private Pair<byte[], byte[]> sendRandomOt() {
        // y
        BigInteger y = randNum.nextBigInteger(getDhModulus());
        // A
        T A = this.getGenerator().exponentiation(y);
        network.send(otherId, A.toByteArray());
        byte[] rBytes = network.receive(otherId);
        // U
        T U = decodeElement(rBytes);

        byte[] k0Hash = new byte[mac.getMacSize()];
        byte[] k1Hash = new byte[mac.getMacSize()];

        byte[] aBytes = A.toByteArray();

        mac.update(aBytes, 0, aBytes.length);
        // (U * B^(0))^y == U^y
        byte[] key0 = U.exponentiation(y).toByteArray();
        mac.update(key0, 0, key0.length);
        mac.doFinal(k0Hash, 0);
        mac.reset();

        mac.update(aBytes, 0, aBytes.length);
        // (U * B^(-1))^y
        byte[] key1 = (U.groupOp(A.inverse())).exponentiation(y).toByteArray();
        mac.update(key1, 0, key1.length);
        mac.doFinal(k1Hash, 0);
        mac.reset();

        // sending of the Encrypted messages is done in outer function
        return new Pair<>(k0Hash, k1Hash);
    }


    /**
     * Needed for Chou-Orlandi
     *
     * Hashing to finite fields according to [1] in point 5.2
     * [1] https://tools.ietf.org/html/draft-irtf-cfrg-hash-to-curve-06
     * @return a byte[] intended to create a new element in the desired filed
     */
    private T hashToFieldElement(T element, String DST) {
        byte[] msg = element.toByteArray();
        //security parameter in bits
        int k = 256;
        int L = (int) Math.ceil((Math.ceil(getDhModulus().bitLength()) + k) / 8);

        //start of algorithm, we only need one element, and m is 1, so L = lenInBytes
        int lenInByts = L;
        // start expand_message_xof
        SHAKEDigest xof = new SHAKEDigest(256);
        xof.update(msg, 0, msg.length);
        xof.update(I2OSP(lenInByts, 2), 0, 2);
        xof.update(I2OSP(DST.getBytes(StandardCharsets.UTF_8).length, 1), 0, 1);
        xof.update(DST.getBytes(StandardCharsets.UTF_8), 0, DST.getBytes(StandardCharsets.UTF_8).length);
        byte[] pseudoRandomBytes = new byte[lenInByts];
        xof.doFinal(pseudoRandomBytes, 0, lenInByts);
        // end expand_message_xof
        BigInteger randBigInt = new BigInteger(1, pseudoRandomBytes);
        return multiplyWithGenerator(randBigInt);
    }
}
