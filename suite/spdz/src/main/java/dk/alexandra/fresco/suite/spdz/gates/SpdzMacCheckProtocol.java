package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.utils.Util;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpdzMacCheckProtocol implements Protocol {

    private SecureRandom rand;
    private MessageDigest digest;
    private List<BigInteger> as;
    private List<SpdzElement> closedValues;
    private SpdzStorage storage;
    private BigInteger s;
    private int round = 0;
    private ProtocolProducer pp;
    private Map<Integer, BigInteger> commitments;

    public SpdzMacCheckProtocol(SecureRandom rand, MessageDigest digest, SpdzStorage storage, Map<Integer, BigInteger> commitments) {
        this.rand = rand;
        this.digest = digest;
        this.storage = storage;
        this.commitments = commitments;
        if (commitments != null)
            this.round = 1;
        else {
            this.commitments = new HashMap<>();
        }
    }

    @Override
    public int getNextProtocols(NativeProtocol[] protocols, int pos) {

        if (pp == null) {
            if (round == 0) {
                BigInteger s = new BigInteger(Util.getModulus().bitLength(), rand).mod(Util.getModulus());
                SpdzCommitment commitment = new SpdzCommitment(digest, s, rand);
                Map<Integer, BigInteger> comms = new HashMap<Integer, BigInteger>();
                SpdzCommitProtocol comm = new SpdzCommitProtocol(commitment, comms);
                SpdzOpenCommitProtocol open = new SpdzOpenCommitProtocol(commitment, comms, commitments);

                pp = new SequentialProtocolProducer(comm, open);
            } else if (round == 1) {
                BigInteger alpha = storage.getSSK();
                this.as = storage.getOpenedValues();
                this.closedValues = storage.getClosedValues();

                // Add all s's to get the common random value:
                s = BigInteger.ZERO;
                for (BigInteger otherS : commitments.values()) {
                    s = s.add(otherS);
                }

                int t = as.size();

                BigInteger[] rs = new BigInteger[t];
                MessageDigest H = new Util().getHashFunction();
                BigInteger r_temp = s;
                for (int i = 0; i < t; i++) {
                    r_temp = new BigInteger(H.digest(r_temp.toByteArray())).mod(Util.getModulus());
                    rs[i] = r_temp;
                }
                BigInteger a = BigInteger.ZERO;
                int index = 0;
                for (BigInteger aa : as) {
                    a = a.add(aa.multiply(rs[index++])).mod(Util.getModulus());
                }

                // compute gamma_i as the sum of all MAC's on the opened values times
                // r_j.
                if (closedValues.size() != t) {
                    throw new MPCException(
                            "Amount of closed values does not equal the amount of partially opened values. Aborting!");
                }
                BigInteger gamma = BigInteger.ZERO;
                index = 0;
                for (SpdzElement c : closedValues) {
                    gamma = gamma.add(rs[index++].multiply(c.getMac())).mod(Util.getModulus());
                }

                // compute delta_i as: gamma_i - alpha_i*a
                BigInteger delta = gamma.subtract(alpha.multiply(a)).mod(Util.getModulus());
                // Commit to delta and open it afterwards
                SpdzCommitment commitment = new SpdzCommitment(digest, delta, rand);
                Map<Integer, BigInteger> comms = new HashMap<Integer, BigInteger>();
                SpdzCommitProtocol comm = new SpdzCommitProtocol(commitment, comms);
                commitments = new HashMap<Integer, BigInteger>();
                SpdzOpenCommitProtocol open = new SpdzOpenCommitProtocol(commitment, comms, commitments);

                pp = new SequentialProtocolProducer(comm, open);
            } else if (round == 2) {
                BigInteger deltaSum = BigInteger.ZERO;
                for (BigInteger d : commitments.values()) {
                    deltaSum = deltaSum.add(d);
                }
                deltaSum = deltaSum.mod(Util.getModulus());
                if (!deltaSum.equals(BigInteger.ZERO)) {
                    throw new MPCException("The sum of delta's was not 0. Someone was corrupting something amongst " + as.size()
                            + " macs. Sum was " + deltaSum.toString() + " Aborting!");
                }
                // clean up store before returning to evaluating such that we only
                // evaluate the next macs, not those we already checked.
                this.storage.reset();
                pp = new SequentialProtocolProducer();
            }
        }
        if (pp.hasNextProtocols()) {
            pos = pp.getNextProtocols(protocols, pos);
        } else if (!pp.hasNextProtocols()) {
            round++;
            pp = null;
        }
        return pos;
    }

    @Override
    public boolean hasNextProtocols() {
        return round < 3;
    }

    @Override
    public Value[] getInputValues() {
        return null;
    }

    @Override
    public Value[] getOutputValues() {
        return null;
    }


}
