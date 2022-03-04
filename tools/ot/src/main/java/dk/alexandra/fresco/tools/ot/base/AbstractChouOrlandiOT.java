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
import org.bouncycastle.crypto.digests.SHAKEDigest;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.bouncycastle.pqc.math.linearalgebra.BigEndianConversions.I2OSP;

/**
 * Uses Chou-Orlandi with fixes as seen in https://eprint.iacr.org/2017/1011
 */
public abstract class AbstractChouOrlandiOT<T extends InterfaceOtElement<T>> implements Ot {

    private static final String HASH_ALGORITHM = "SHA-256";
    private final int otherId;
    private final Network network;
    protected final Drng randNum;

    private final MessageDigest hashDigest;


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
        this.hashDigest = ExceptionConverter.safe(() -> MessageDigest.getInstance(HASH_ALGORITHM),
                "Missing secure, hash function which is dependent in this library");
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
        // S
        T S = this.decodeElement(network.receive(otherId));
        // T = G(S)
        T T = hashToFieldElement(S, "FRESCO|ChouOrlandi|OT");
        // R = T^c*g^x
        // if c = 0 -> R = g^x
        // if c = 1 -> R = T*g^x
        BigInteger x = randNum.nextBigInteger(getDhModulus());
        T R;
        if (choiceBit == false) {
            R = T.exponentiation(BigInteger.ZERO).groupOp(getGenerator().exponentiation(x));
        } else {
            R = T.exponentiation(BigInteger.ONE).groupOp(getGenerator().exponentiation(x));
        }
        network.send(otherId, R.toByteArray());

        // k = H(S, R, S^x)
        byte[] key;
        hashDigest.update(S.toByteArray());
        hashDigest.update(R.toByteArray());
        hashDigest.update(S.exponentiation(x).toByteArray());
        key = hashDigest.digest();
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
        // S
        T S = this.getGenerator().exponentiation(y);
        network.send(otherId, S.toByteArray());
        // T = G(S)
        T T = hashToFieldElement(S, "FRESCO|ChouOrlandi|OT");
        byte[] rBytes = network.receive(otherId);
        // R
        T R = decodeElement(rBytes);

        byte[] k0Hash, k1Hash;
        hashDigest.update(S.toByteArray());
        hashDigest.update(R.toByteArray());
        // R^y*T^(-jy) with j = 0:
        // R^y * T^(-0y) == R^y
        hashDigest.update(R.exponentiation(y).toByteArray());
        k0Hash = hashDigest.digest();

        hashDigest.update(S.toByteArray());
        hashDigest.update(R.toByteArray());
        // R^y*T^(-jy) with j = 1:
        // R^y * T^(-1y) == (R + (-T)) * y
        hashDigest.update((R.groupOp(T.inverse())).exponentiation(y).toByteArray());
        k1Hash = hashDigest.digest();

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
