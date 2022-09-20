package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;

import java.math.BigInteger;
import javax.crypto.spec.DHParameterSpec;

public class BigIntChouOrlandi extends AbstractChouOrlandiOT<BigIntElement> {
    private final BigInteger subgroupOrder;
    /**
     * The generator of the Diffie-Hellman group used in the OT.
     */
    private final BigInteger generator;

    public BigIntChouOrlandi(int otherId, Drbg randBit, Network network) {
        super(otherId, randBit, network);
        DHParameterSpec params = DhParameters.getStaticDhParams();
        // The modulus, P, MUST be a safe prime, so we set the subgroup order to be q=2p-1
        this.subgroupOrder = params.getP().subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
        this.generator = params.getG();
    }

    @Override
    BigIntElement decodeElement(byte[] bytes) {
        return new BigIntElement(new BigInteger(1, bytes), this.subgroupOrder);
    }

    @Override
    BigInteger getSubgroupOrder() {
        return this.subgroupOrder;
    }

    @Override
    BigIntElement getGenerator() {
        return new BigIntElement(this.generator, this.subgroupOrder);
    }

}

