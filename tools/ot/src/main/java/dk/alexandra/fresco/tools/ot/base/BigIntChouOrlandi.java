package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;

import java.math.BigInteger;
import java.security.MessageDigest;
import javax.crypto.spec.DHParameterSpec;

public class BigIntChouOrlandi extends AbstractChouOrlandiOT<BigIntElement> {

    private static final String HASH_ALGORITHM = "SHA-256";
    private final MessageDigest hashDigest;


    /**
     * The modulus of the Diffie-Hellman group used in the OT.
     */
    private final BigInteger dhModulus;
    /**
     * The generator of the Diffie-Hellman group used in the OT.
     */
    private final BigInteger dhGenerator;

    public BigIntChouOrlandi(int otherId, Drbg randBit, Network network) {
        super(otherId, randBit, network);
        DHParameterSpec params = DhParameters.getStaticDhParams();
        this.dhModulus = params.getP();
        this.dhGenerator = params.getG();
        this.hashDigest = ExceptionConverter.safe(() -> MessageDigest.getInstance(HASH_ALGORITHM),
                "Missing secure, hash function which is dependent in this library");
    }

    @Override
    BigIntElement decodeElement(byte[] bytes) {
        return new BigIntElement(new BigInteger(bytes), this.dhModulus);
    }

    @Override
    BigInteger getDhModulus() {
        return this.dhModulus;
    }

    @Override
    BigIntElement multiplyWithGenerator(BigInteger input) {
        return new BigIntElement(this.dhGenerator.multiply(input).mod(this.dhModulus), this.dhModulus);
    }

    @Override
    BigIntElement getGenerator() {
        return new BigIntElement(this.dhGenerator, this.dhModulus);
    }

}

