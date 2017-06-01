/*******************************************************************************
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
package dk.alexandra.fresco.lib.statistics;

import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.builder.ComparisonProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

/**
 * Protocol for performing credit rating.
 * 
 * Given a dataset (a vector of values)
 * and a credit rating function (a set of intervals for each value)
 * will calculate the combined score.
 *
 */
public class CreditRater implements Application{

	private static final long serialVersionUID = 7679664125131997196L;
	private List<SInt> values;
	private List<List<SInt>> intervals;
	private List<List<SInt>> intervalScores;  

	private SInt score;
	
	/**
	 * @throws MPCException
	 */
	public CreditRater(
			List<SInt> values, List<List<SInt>> intervals, List<List<SInt>> intervalScores) throws MPCException {
		this.values = values;
		this.intervals = intervals;
		this.intervalScores = intervalScores;
		if(!consistencyCheck()){
			throw new MPCException("Inconsistent data");
		}
	}
	
	/**
	 * Verify that the input values are consistent, i.e.
	 * the there is an interval for each value
	 * @return If the input is consistent.
	 */
	private boolean consistencyCheck() {
	  if (this.values.size() != this.intervals.size()) {
	    return false;
	  }
	  if (this.intervals.size() != (this.intervalScores.size())) {
	    return false;
	  }
		return true;
	}

	@Override
	public ProtocolProducer prepareApplication(ProtocolFactory provider) {
    OmniBuilder omniBuilder = new OmniBuilder(provider);


    SInt[] individualScores = new SInt[this.values.size()]; 
    
    for (int i = 0; i< this.values.size(); i++) {
      individualScores[i] = computeIntervalScore(this.values.get(i), 
          this.intervals.get(i), 
          this.intervalScores.get(i), 
          omniBuilder);
    }
    if(individualScores.length == 1) {
      this.score = individualScores[0];
    } else {
      this.score = omniBuilder.getNumericProtocolBuilder().sum(individualScores);
    }
    SequentialProtocolProducer seq = new SequentialProtocolProducer();

    seq.append(omniBuilder.getProtocol());
    
    return seq;
	}
	
	/**
	 * Given a value and scores for an interval, will lookup the score for
	 * the value.   
	 * @param value The value to lookup
	 * @param interval The interval definition
	 * @param scores The scores for each interval
	 * @param builder The builder used to construct operators
	 * @return The score for the lookup
	 */
	private SInt computeIntervalScore(SInt value, List<SInt> interval, List<SInt> scores, OmniBuilder builder) {
	  List<SInt> intermediateScores = new ArrayList<SInt>(scores.size()); 

	  ComparisonProtocolBuilder comparisonBuilder = builder.getComparisonProtocolBuilder();    
    NumericProtocolBuilder numericBuilder = builder.getNumericProtocolBuilder();
    
    builder.beginSeqScope();
	  builder.beginParScope();
	  
	  // Compare if "x <= the n interval definitions"
	  List<SInt> comparisons = new ArrayList<SInt>();
	  for(int i = 0; i< interval.size(); i++) {
	    comparisons.add(comparisonBuilder.compare(value, interval.get(i)));
	  }
	  SInt one = numericBuilder.getSInt(1);
    builder.endCurScope();
    
	  // Add "x > last interval definition" to comparisons
	  comparisons.add(numericBuilder.sub(one, comparisons.get(comparisons.size()-1)));
	  
	  //Comparisons now contain if x <= each definition and if x>= last definition
    builder.beginParScope();	   //need to verify if this is "legal"
	  intermediateScores.add(numericBuilder.mult(comparisons.get(0), scores.get(0)));
	  for(int i = 1; i < scores.size()-1; i++) {
	    SInt hit = numericBuilder.sub(comparisons.get(i), comparisons.get(i-1));
	    intermediateScores.add(numericBuilder.mult(hit, scores.get(i)));
	  }
	  intermediateScores.add(numericBuilder.mult(comparisons.get(scores.size()-1), scores.get(scores.size()-1))); 
	  builder.endCurScope();	  
	  
	  SInt sum = builder.getNumericProtocolBuilder().sum((SInt[]) intermediateScores.toArray(new SInt[intermediateScores.size()]));
    builder.endCurScope();
    return sum;
  }

	/**
	 * 
	 * @return
	 */
  public SInt getResult(){
		return this.score;
	}
	
}
