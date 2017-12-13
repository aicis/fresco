package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.commitment.CommitmentSerializer;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.PaddingPrg;

public class MascotResourcePoolImpl implements MascotResourcePool {

  Integer myId;
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

  public MascotResourcePoolImpl(Integer myId, List<Integer> partyIds, BigInteger modulus,
      int modBitLength, int lambdaSecurityParam, int prgSeedLength, int numLeftFactors) {
    super();
    this.myId = myId;
    this.partyIds = partyIds;
    this.modulus = modulus;
    this.modBitLength = modBitLength;
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.numLeftFactors = numLeftFactors;
    this.prgSeedLength = prgSeedLength;
    this.localSampler = new PaddingPrg(new StrictBitVector(prgSeedLength, new SecureRandom()));
    this.fieldElementSerializer = new FieldElementSerializer(modulus, modBitLength);
    this.strictBitVectorSerializer = new StrictBitVectorSerializer();
    this.commitmentSerializer = new CommitmentSerializer();
    try {
      this.messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

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
  public BigIntegerSerializer getSerializer() {
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
  public int getMyId() {
    return myId;
  }

  @Override
  public int getNoOfParties() {
    return partyIds.size();
  }

  @Override
  public Drbg getRandomGenerator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getPrgSeedLength() {
    return prgSeedLength;
  }

}
