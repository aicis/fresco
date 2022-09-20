package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECCurve;

import java.math.BigInteger;
import java.security.Security;

public class ECChouOrlandi extends AbstractChouOrlandiOT<ECElement>{

    private final BigInteger curveOrder;
    private final org.bouncycastle.math.ec.ECPoint dhGenerator;

    private final ECCurve curve;

    public ECChouOrlandi(int otherId, Drbg randBit, Network network) {
        super(otherId, randBit, network);
        Security.addProvider(new BouncyCastleProvider());
        X9ECParameters ecP = CustomNamedCurves.getByName("curve25519");
        this.curve = ecP.getCurve();
        this.curveOrder = curve.getOrder();
        this.dhGenerator = ecP.getG();
    }

    @Override
    ECElement decodeElement(byte[] bytes) {
        // We expect BouncyCastle to verify that the point is correct.
        return new ECElement(this.curve.decodePoint(bytes));
    }

    @Override
    ECElement getGenerator() {
        return new ECElement(this.dhGenerator);
    }

    @Override
    BigInteger getSubgroupOrder() {
        return this.curveOrder;
    }

}
