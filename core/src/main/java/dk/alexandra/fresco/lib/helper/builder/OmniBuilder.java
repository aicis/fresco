/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.helper.builder;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.LegacyNumericProducer;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SIntFactory;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactoryImpl;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactory;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.generic.IOIntProtocolFactory;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactory;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactory;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import dk.alexandra.fresco.lib.math.integer.stat.StatisticsFactory;
import dk.alexandra.fresco.lib.math.integer.stat.StatisticsFactoryImpl;

/**
 * This builder can be used for all possible functionality in FRESCO. It encapsulates all other
 * builders and creates them lazily. This means that application creators are not forced to create
 * all sorts of factories and cast the factory manually. This is done for the user whenever
 * requested.
 *
 * Note that using this builder does not mean that the application will always be sound. It still
 * requires that the underlying protocol suite used supports the factory methods that you use.
 *
 * This builder is the parent of all it's builders, meaning that all builders use this builders
 * internal protocol stack. Thus, all calls on any builder that adds protocols or new scopes will
 * use this builders stack. This means an application development user just writes the application
 * linearly without thinking about which builder stack is now used.
 *
 * @author Kasper
 */
public class OmniBuilder extends AbstractProtocolBuilder {

  private ProtocolFactory factory;
  private BasicLogicBuilder basicLogicBuilder;
  private NumericIOBuilder numericIOBuilder;
  private NumericProtocolBuilder numericProtocolBuilder;
  private AdvancedNumericBuilder advancedNumericBuilder;
  private ComparisonProtocolBuilder comparisonProtocolBuilder;
  private StatisticsProtocolBuilder statisticsProtocolBuilder;
  private SymmetricEncryptionBuilder symmetricEncryptionBuilder;
  private UtilityBuilder utilityBuilder;

  //Used in various protocols - typically for comparisons.
  //TODO: Better explanation as to what this is, and what it means for performance/security.
  private final int statisticalSecurityParameter;

  /**
   * Creates a builder that can create all currently known functions used in FRESCO.
   * This constructor has a default statistical security parameter of 80.
   * If it should be different, use the other constructor.
   *
   * @param factory A factory that supports all the functions that you want to call using this
   * builder.
   */
  public OmniBuilder(ProtocolFactory factory) {
    this(factory, 80);
  }

  /**
   * Creates a builder that can create all currently known functions used in FRESCO.
   */
  public OmniBuilder(ProtocolFactory factory, int statisticalSecurityParameter) {
    this.factory = factory;
    this.statisticalSecurityParameter = statisticalSecurityParameter;
  }

  public int getStatisticalSecurityParameter() {
    return statisticalSecurityParameter;
  }

  /**
   * Builder for creating boolean circuits. It contains all boolean functionality that FRESCO
   * provides. Currently expects that the constructor given factory implements all interfaces listed
   * below: -  BasicLogicFactory
   */
  public BasicLogicBuilder getBasicLogicBuilder() {
    if (basicLogicBuilder == null) {
      basicLogicBuilder = new BasicLogicBuilder((AbstractBinaryFactory) factory);
      basicLogicBuilder.setParentBuilder(this);
    }
    return basicLogicBuilder;
  }

  /**
   * Builder used for inputting and outputting values. Note that inputting values using this builder
   * often requires at least two different applications or a switch on the party ID. Currently
   * expects that the constructor given factory implements all interfaces listed below: -
   * IOIntProtocolFactory - SIntFactory - OIntFactory
   *
   * Which is a subset of the BasicNumericFactory interface.
   */
  public NumericIOBuilder getNumericIOBuilder() {
    if (numericIOBuilder == null) {
      numericIOBuilder = new NumericIOBuilder(
          (IOIntProtocolFactory & SIntFactory & OIntFactory) factory);
      numericIOBuilder.setParentBuilder(this);
    }
    return numericIOBuilder;
  }


  /**
   * Builder used for basic numeric functionality such as add, mult and sub.
   * Currently expects that the constructor given factory implements all interfaces listed below:
   * - BasicNumericFactory
   */
  public NumericProtocolBuilder getNumericProtocolBuilder() {
    if (numericProtocolBuilder == null) {
      numericProtocolBuilder = new NumericProtocolBuilder((BasicNumericFactory) factory);
      numericProtocolBuilder.setParentBuilder(this);
    }
    return numericProtocolBuilder;
  }

  public AdvancedNumericBuilder getAdvancedNumericBuilder() {
    if (advancedNumericBuilder == null) {
      SIntFactory intFactory = (SIntFactory) factory;
      DivisionFactory divisionFactory = getDivisionFactory();
      advancedNumericBuilder = new AdvancedNumericBuilder(divisionFactory, intFactory);
      advancedNumericBuilder.setParentBuilder(this);
    }
    return advancedNumericBuilder;
  }

  private DivisionFactory getDivisionFactory() {
    BasicNumericFactory basicNumericFactory = (BasicNumericFactory) factory;
    LocalInversionFactory localInversionFactory = (LocalInversionFactory) factory;
    NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) factory;
    RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(
        basicNumericFactory, preprocessedNumericBitFactory);
    RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(basicNumericFactory,
        randomAdditiveMaskFactory, localInversionFactory);
    IntegerToBitsFactory integerToBitsFactory = new IntegerToBitsFactoryImpl(basicNumericFactory,
        rightShiftFactory);
    BitLengthFactory bitLengthFactory = new BitLengthFactoryImpl(basicNumericFactory,
        integerToBitsFactory);
    ExponentiationFactory exponentiationFactory = new ExponentiationFactoryImpl(basicNumericFactory,
        integerToBitsFactory);
    ComparisonProtocolFactory comparisonFactory = getComparisonProtocolFactory();
    return new DivisionFactoryImpl(basicNumericFactory, rightShiftFactory, bitLengthFactory,
        exponentiationFactory, comparisonFactory);
  }

  /**
   * Builder used to do comparisons. Currently expects that the constructor given factory implements
   * all interfaces listed below: - BasicNumericFactory - LocalInversionFactory -
   * PreprocessedNumericBitFactory - ExpFromOIntFactory - PreprocessedExpPipeFactory
   */
  public ComparisonProtocolBuilder getComparisonProtocolBuilder() {
    if (comparisonProtocolBuilder == null) {
      BasicNumericFactory bnf = (BasicNumericFactory) factory;
      ComparisonProtocolFactory comFactory = getComparisonProtocolFactory();
      comparisonProtocolBuilder = new ComparisonProtocolBuilder(comFactory, bnf);
      comparisonProtocolBuilder.setParentBuilder(this);
    }
    return comparisonProtocolBuilder;
  }

  private ComparisonProtocolFactory getComparisonProtocolFactory() {
    BasicNumericFactory bnf = (BasicNumericFactory) factory;
    LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
    NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
    ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
    PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
    return new ComparisonProtocolFactoryImpl(statisticalSecurityParameter, bnf, localInvFactory,
        numericBitFactory, expFromOIntFactory, expFactory,
        new LegacyNumericProducer<>(bnf));
  }


  /**
   * Builder used for statistical functionalities such as mean, variance, covariance.
   * Currently expects that the constructor given factory implements all interfaces listed below:
   * - BasicNumericFactory
   * - LocalInversionFactory
   * - PreprocessedNumericBitFactory
   */
  public StatisticsProtocolBuilder getStatisticsProtocolBuilder() {
    if (statisticsProtocolBuilder == null) {
      BasicNumericFactory basicNumericFactory = (BasicNumericFactory) factory;
      DivisionFactory euclidianDivisionFactory = getDivisionFactory();
      StatisticsFactory statFac = new StatisticsFactoryImpl(basicNumericFactory,
          euclidianDivisionFactory);
      statisticsProtocolBuilder = new StatisticsProtocolBuilder(statFac, basicNumericFactory);
      statisticsProtocolBuilder.setParentBuilder(this);
    }
    return statisticsProtocolBuilder;
  }


  /**
   * Builder used for doing symmetric encryption within arithmetic fields.
   * Currently expects that the constructor given factory implements all interfaces listed below:
   * - BasicNumericFactory
   *
   * @return A builder that constructs symmetric encryption protocols
   */
  public SymmetricEncryptionBuilder getSymmetricEncryptionBuilder() {
    if (symmetricEncryptionBuilder == null) {
      BasicNumericFactory basicNumericFactory = (BasicNumericFactory) factory;
      symmetricEncryptionBuilder = new SymmetricEncryptionBuilder(basicNumericFactory);
      symmetricEncryptionBuilder.setParentBuilder(this);
    }
    return symmetricEncryptionBuilder;
  }

  /**
   * Builder used primarily for debug purposes, so be careful including this in production code.
   * Currently expects that the constructor given factory implements one of the interfaces listed
   * below: - BasicNumericFactory - BasicLogicFactory
   *
   * @return A builder that constructs primarily debug protocols.
   */
  public UtilityBuilder getUtilityBuilder() {
    if (utilityBuilder == null) {
      utilityBuilder = new UtilityBuilder(factory);
      utilityBuilder.setParentBuilder(this);
    }
    return utilityBuilder;
  }

  @Override
  public void addProtocolProducer(ProtocolProducer pp) {
    append(pp);
  }

}
