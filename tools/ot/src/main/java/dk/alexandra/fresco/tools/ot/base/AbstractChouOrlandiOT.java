package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.*;
import dk.alexandra.fresco.tools.ot.otextension.PseudoOtp;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.macs.HMac;

import java.math.BigInteger;

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

    abstract BigInteger getSubgroupOrder();

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
        BigInteger x = randNum.nextBigInteger(getSubgroupOrder());
        T U;
        if (choiceBit == false) {
            U = getGenerator().exponentiation(x);
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
        BigInteger y = randNum.nextBigInteger(getSubgroupOrder());
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


}
