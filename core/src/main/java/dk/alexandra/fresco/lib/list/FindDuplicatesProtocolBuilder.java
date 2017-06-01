/**
 * 
 */
package dk.alexandra.fresco.lib.list;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.ComparisonProtocolBuilder;

/**
 * @author mortenvchristiansen
 *
 */
public class FindDuplicatesProtocolBuilder extends ComparisonProtocolBuilder {

	
	
	BasicNumericFactory bnf;
	/**
	 * @param comProvider
	 * @param bnf
	 */
	public FindDuplicatesProtocolBuilder(ComparisonProtocolFactory comProvider, BasicNumericFactory bnf) {
		super(comProvider, bnf);
		this.bnf=bnf;
		
	}

	 private SInt or(SInt a, SInt b) {
	    	SInt result=bnf.getSInt();
	    	append(bnf.getMultProtocol(a, b, result));
	    	SInt result2=bnf.getSInt();
	    	append(bnf.getAddProtocol(a, b, result2));
	    	SInt result3=bnf.getSInt();
	    	append(bnf.getSubtractProtocol(result2, result, result3));
	    	return result3;
	    }
	 
	 /**
	  * annotates list1 with duplicate marks. To annotate list 2 also, run with the lists switched 
	  * If a horisontal join is desired, make sure that both lists are ordered initially, annotate both lists
	  * and update fields by going through tables in lockstep.
	  * @param list1
	  * @param list2
	  */
	 public void findDuplicates(SIntListofTuples list1, SIntListofTuples list2) {
		 beginParScope();
		 for (int i=0;i<list1.size();i++) {
			beginSeqScope();
			for (int j=0;j<list2.size();j++)
				list1.setDuplicate(i, or(list1.getDuplicate(i),compareEqual(list1.getId(i), list2.getId(j))));
			endCurScope();
		 }
		 endCurScope();
	 }
	
	
}
