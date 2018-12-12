package dk.alexandra.fresco.suite.spdz.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.framework.util.TransposeUtils;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;

public class TestSpdzDummyDataSupplier {

  private final List<FieldDefinition> fields = Arrays.asList(
      new FieldDefinitionBigInteger(new ModulusBigInteger(new BigInteger("251"))),
      new FieldDefinitionBigInteger(
          new ModulusBigInteger(new BigInteger("340282366920938463463374607431768211283"))),
      new FieldDefinitionBigInteger(new ModulusBigInteger(new BigInteger(
          "2582249878086908589655919172003011874329705792829223512830659356540647622016841"
              + "194629645353280137831435903171972747493557")))
  );

  private List<SpdzDummyDataSupplier> setupSuppliers(int noOfParties,
      FieldDefinition fieldDefinition) {
    return setupSuppliers(noOfParties, 200, fieldDefinition);
  }

  private List<SpdzDummyDataSupplier> setupSuppliers(int noOfParties,
      int expPipeLength, FieldDefinition fieldDefinition) {
    List<SpdzDummyDataSupplier> suppliers = new ArrayList<>(noOfParties);
    Random random = new Random();
    for (int i = 0; i < noOfParties; i++) {
      BigInteger macKeyShare =
          new BigInteger(fieldDefinition.getModulus().bitLength(), random)
              .mod(fieldDefinition.getModulus());
      suppliers.add(
          new SpdzDummyDataSupplier(i + 1, noOfParties,
              fieldDefinition,
              macKeyShare,
              expPipeLength));
    }
    return suppliers;
  }

  private FieldElement getMacKeyFromSuppliers(
      List<SpdzDummyDataSupplier> suppliers, FieldDefinition fieldDefinition) {
    FieldElement macKey = fieldDefinition.createElement(0);
    for (SpdzDummyDataSupplier supplier : suppliers) {
      macKey = macKey.add(supplier.getSecretSharedKey());
    }
    return macKey;
  }

  private void testGetNextTriple(int noOfParties, FieldDefinition fieldDefinition) {
    List<SpdzDummyDataSupplier> suppliers = setupSuppliers(noOfParties, fieldDefinition);
    FieldElement macKey = getMacKeyFromSuppliers(suppliers, fieldDefinition);
    List<SpdzTriple> triples = new ArrayList<>(noOfParties);
    for (SpdzDummyDataSupplier supplier : suppliers) {
      triples.add(supplier.getNextTriple());
    }
    SpdzTriple recombined = recombineTriples(triples);
    assertTripleValid(recombined, macKey);
  }

  private void testGetNextTriple(int noOfParties) {
    for (FieldDefinition definition : fields) {
      testGetNextTriple(noOfParties, definition);
    }
  }

  private void testGetNextInputMask(int noOfParties, int towardParty,
      FieldDefinition fieldDefinition) {
    List<SpdzDummyDataSupplier> suppliers = setupSuppliers(noOfParties, fieldDefinition);
    FieldElement macKey = getMacKeyFromSuppliers(suppliers, fieldDefinition);
    List<SpdzInputMask> masks = new ArrayList<>(noOfParties);
    for (SpdzDummyDataSupplier supplier : suppliers) {
      masks.add(supplier.getNextInputMask(towardParty));
    }
    FieldElement realValue = null;
    List<SpdzSInt> shares = new ArrayList<>(noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      SpdzInputMask spdzInputMask = masks.get(i);
      if (i + 1 != towardParty) {
        assertEquals(null, spdzInputMask.getRealValue());
      } else {
        realValue = spdzInputMask.getRealValue();
      }
      shares.add(spdzInputMask.getMask());
    }
    SpdzSInt recombined = recombine(shares);
    assertMacCorrect(recombined, macKey);
    assertEquals(realValue, recombined.getShare());
  }

  private void testGetNextInputMask(int noOfParties, int towardParty) {
    for (FieldDefinition field : fields) {
      testGetNextInputMask(noOfParties, towardParty, field);
    }
  }

  private void testGetNextBit(int noOfParties, FieldDefinition definition) {
    List<SpdzDummyDataSupplier> suppliers = setupSuppliers(noOfParties, definition);
    FieldElement macKey = getMacKeyFromSuppliers(suppliers, definition);
    List<SpdzSInt> bitShares = new ArrayList<>(noOfParties);
    for (SpdzDummyDataSupplier supplier : suppliers) {
      bitShares.add(supplier.getNextBit());
    }
    SpdzSInt recombined = recombine(bitShares);
    assertMacCorrect(recombined, macKey);
    FieldElement value = recombined.getShare();
    BigInteger actualResult = definition.convertRepresentation(value);
    assertTrue("Value not a bit " + actualResult,
        actualResult.equals(BigInteger.ZERO) || actualResult.equals(BigInteger.ONE));
  }

  private void testGetNextBit(int noOfParties) {
    for (FieldDefinition field : fields) {
      testGetNextBit(noOfParties, field);
    }
  }

  private void testGetNextRandomFieldElement(int noOfParties, FieldDefinition definition) {
    List<SpdzDummyDataSupplier> suppliers = setupSuppliers(noOfParties, definition);
    FieldElement macKey = getMacKeyFromSuppliers(suppliers, definition);
    List<SpdzSInt> bitShares = new ArrayList<>(noOfParties);
    for (SpdzDummyDataSupplier supplier : suppliers) {
      bitShares.add(supplier.getNextRandomFieldElement());
    }
    SpdzSInt recombined = recombine(bitShares);
    assertMacCorrect(recombined, macKey);
    // sanity check not zero (with 251, that is actually not unlikely enough)
    if (!definition.getModulus().equals(new BigInteger("251"))) {
      FieldElement value = recombined.getShare();
      BigInteger bigIntegerValue = definition.convertRepresentation(value);
      assertFalse("Random value was 0 ", bigIntegerValue.equals(BigInteger.ZERO));
    }
  }

  private void testGetNextRandomFieldElement(int noOfParties) {
    for (FieldDefinition fieldDefinition : fields) {
      testGetNextRandomFieldElement(noOfParties, fieldDefinition);
    }
  }

  private void testGetNextExpPipe(int noOfParties, FieldDefinition definition,
      int expPipeLength) {
    List<SpdzDummyDataSupplier> suppliers = setupSuppliers(noOfParties, definition);
    FieldElement macKey = getMacKeyFromSuppliers(suppliers, definition);
    List<SpdzSInt[]> expPipes = new ArrayList<>(noOfParties);
    for (SpdzDummyDataSupplier supplier : suppliers) {
      expPipes.add(supplier.getNextExpPipe());
    }
    for (SpdzSInt[] expPipe : expPipes) {
      assertEquals(expPipeLength + 1, expPipe.length);
    }
    List<List<SpdzSInt>> unwrapped = expPipes.stream()
        .map(pipe -> Arrays.stream(pipe).collect(Collectors.toList()))
        .collect(Collectors.toList());
    List<SpdzSInt> recombined = recombineExpPipe(unwrapped);
    assertExpPipeValid(recombined, macKey, definition);
  }

  private void testGetNextExpPipe(int noOfParties) {
    for (FieldDefinition field : fields) {
      testGetNextExpPipe(noOfParties, field, 200);
    }
  }

  @Test
  public void testGetNextTriple() {
    testGetNextTriple(2);
    testGetNextTriple(3);
    testGetNextTriple(5);
  }

  @Test
  public void testGetNextExpPipe() {
    testGetNextExpPipe(2);
    testGetNextExpPipe(3);
    testGetNextExpPipe(5);
  }

  @Test
  public void testGetNextInputMask() {
    List<Integer> partyCounts = Arrays.asList(2, 3, 5);
    for (int partyCount : partyCounts) {
      for (int i = 0; i < partyCount; i++) {
        testGetNextInputMask(partyCount, i + 1);
      }
    }
  }

  @Test
  public void testGetNextBit() {
    testGetNextBit(2);
    testGetNextBit(3);
    testGetNextBit(5);
  }

  @Test
  public void testGetNextRandomFieldElement() {
    testGetNextRandomFieldElement(2);
    testGetNextRandomFieldElement(3);
    testGetNextRandomFieldElement(5);
  }

  @Test
  public void testGetters() {
    FieldDefinition fieldDefinition = fields.get(0);
    SpdzDummyDataSupplier supplier =
        new SpdzDummyDataSupplier(1, 2, fieldDefinition, BigInteger.ONE);
    assertEquals(fields.get(0).getModulus(), supplier.getModulus());
    assertEquals(BigInteger.ONE,
        fieldDefinition.convertRepresentation(supplier.getSecretSharedKey()));
  }

  private SpdzSInt recombine(List<SpdzSInt> shares) {
    return shares.stream().reduce(SpdzSInt::add).get();
  }

  private List<SpdzSInt> recombineExpPipe(List<List<SpdzSInt>> expPipeShares) {
    return TransposeUtils.transpose(expPipeShares).stream()
        .map(this::recombine)
        .collect(Collectors.toList());
  }

  private SpdzTriple recombineTriples(List<SpdzTriple> triples) {
    List<SpdzSInt> left = new ArrayList<>(triples.size());
    List<SpdzSInt> right = new ArrayList<>(triples.size());
    List<SpdzSInt> product = new ArrayList<>(triples.size());
    for (SpdzTriple triple : triples) {
      left.add(triple.getA());
      right.add(triple.getB());
      product.add(triple.getC());
    }
    return new SpdzTriple(recombine(left), recombine(right), recombine(product));
  }

  private void assertMacCorrect(SpdzSInt recombined, FieldElement macKey) {
    FieldElement share = recombined.getShare().multiply(macKey);
    assertEquals(share, recombined.getMac());
  }

  private void assertTripleValid(SpdzTriple recombined, FieldElement macKey) {
    assertMacCorrect(recombined.getA(), macKey);
    assertMacCorrect(recombined.getB(), macKey);
    assertMacCorrect(recombined.getC(), macKey);

    FieldElement copy = recombined.getA().getShare().multiply(recombined.getB().getShare());
    // check that a * b = c
    assertEquals(recombined.getC().getShare(), copy
    );
  }

  private void assertExpPipeValid(List<SpdzSInt> recombined, FieldElement macKey,
      FieldDefinition definition) {
    for (SpdzSInt element : recombined) {
      assertMacCorrect(element, macKey);
    }
    List<FieldElement> values = recombined.stream().map(SpdzSInt::getShare)
        .collect(Collectors.toList());
    FieldElement inverted = values.get(0);
    FieldElement first = values.get(1);
    BigInteger bigInteger =
        definition.convertRepresentation(first).modInverse(definition.getModulus());
    assertEquals(definition.convertRepresentation(inverted), bigInteger);
    for (int i = 1; i < values.size(); i++) {
      BigInteger expected = definition.convertRepresentation(first)
          .modPow(BigInteger.valueOf(i), definition.getModulus());
      assertEquals(expected, definition.convertRepresentation(values.get(i)));
    }
  }
}
