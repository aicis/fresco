/**
 * 
 */
package dk.alexandra.fresco.lib.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.alexandra.fresco.framework.value.SInt;

/**
 * @author mortenvchristiansen
 *
 */
public class SIntListofTuples {
  private List<SInt[]> theData = new ArrayList<SInt[]>();
  private List<SInt> duplicate_p = new ArrayList<SInt>();
  
	/**
	 * id is first element in tuple
	 */
  	public final int rowWidth;
  	
  	/**
  	 * 
  	 * @param row the data values (and id in first column)
  	 * @param falseValue an Sint representing zero, to mark the row as non-duplicate.
  	 * @return
  	 */
  	public SIntListofTuples add(SInt[] row, SInt falseValue){
  	    if (row.length!=rowWidth)
  	    	throw new RuntimeException("Row width wrong. Should be "+rowWidth);
  	    theData.add(row);	
  	    duplicate_p.add(falseValue);
  		return this;
  	}
  	
  	public SIntListofTuples remove(int index){
  	    theData.remove(index);	
  	    duplicate_p.remove(index);
  		return this;
  	}
  	
  	public SInt[] get(int index){
  		return theData.get(index);
  	}
  	
  	public SInt getId(int index){
  		return theData.get(index)[0];
  	}
  	
  	public List<SInt[]> getReadOnlyList(){
  		return Collections.unmodifiableList(theData);
  	}
  	
	public SIntListofTuples(int rowWidth) {
		this.rowWidth=rowWidth;
	}
	
	public void setDuplicate(int index,SInt value){
		duplicate_p.set(index,value);
	}
	
	public SInt getDuplicate(int index){
		return duplicate_p.get(index);
	}
	
	public int size(){
		return theData.size();
	}
}