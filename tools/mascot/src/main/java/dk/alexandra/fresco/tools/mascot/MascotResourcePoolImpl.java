package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.SecureSerializer;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.commitment.CommitmentSerializer;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrgImpl;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.BristolRotBatch;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

public class MascotResourcePoolImpl extends ResourcePoolImpl implements MascotResourcePool {

  List<Integer> partyIds;
  BigInteger modulus;
  int modBitLength;
  int lambdaSecurityParam;
  int prgSeedLength;
  int numLeftFactors;
  FieldElementPrg localSampler;
  FieldElementSerializer fieldElementSerializer;
  StrictBitVectorSerializer strictBitVectorSerializer;
  CommitmentSerializer commitmentSerializer;
  MessageDigest messageDigest;

  public MascotResourcePoolImpl(Integer myId, List<Integer> partyIds, Drbg drbg, BigInteger modulus,
      int modBitLength, int lambdaSecurityParam, int prgSeedLength, int numLeftFactors) {
    super(myId, partyIds.size(), drbg);
    this.partyIds = partyIds;
    this.modulus = modulus;
    this.modBitLength = modBitLength;
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.numLeftFactors = numLeftFactors;
    this.prgSeedLength = prgSeedLength;
    this.localSampler = new FieldElementPrgImpl(new StrictBitVector(prgSeedLength, drbg));
    this.fieldElementSerializer = new FieldElementSerializer(modulus, modBitLength);
    this.strictBitVectorSerializer = new StrictBitVectorSerializer();
    this.commitmentSerializer = new CommitmentSerializer();
    this.messageDigest = ExceptionConverter.safe(() -> MessageDigest.getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for Spdz");
  }

  @Override
  public List<Integer> getPartyIds() {
    return partyIds;
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public int getModBitLength() {
    return modBitLength;
  }

  @Override
  public int getLambdaSecurityParam() {
    return lambdaSecurityParam;
  }

  @Override
  public int getNumCandidatesPerTriple() {
    return numLeftFactors;
  }

  @Override
  public FieldElementPrg getLocalSampler() {
    return localSampler;
  }

  @Override
  public FieldElementSerializer getFieldElementSerializer() {
    return fieldElementSerializer;
  }

  @Override
  public StrictBitVectorSerializer getStrictBitVectorSerializer() {
    return strictBitVectorSerializer;
  }

  @Override
  public CommitmentSerializer getCommitmentSerializer() {
    return commitmentSerializer;
  }

  @Override
  public RotBatch<StrictBitVector> createRot(int otherId, Network network) {
    return new BristolRotBatch(getMyId(), otherId, getModBitLength(), getLambdaSecurityParam(),
        getRandomGenerator(), network);

  }

  @Override
  public SecureSerializer<BigInteger> getSerializer() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MessageDigest getMessageDigest() {
    return messageDigest;
  }

  @Override
  public BigInteger convertRepresentation(BigInteger b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Drbg getRandomGenerator() {
    return drbg;
  }

  @Override
  public int getPrgSeedLength() {
    return prgSeedLength;
  }

}
