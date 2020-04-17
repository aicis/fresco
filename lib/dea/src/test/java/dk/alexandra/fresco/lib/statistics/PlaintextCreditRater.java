package dk.alexandra.fresco.lib.statistics;

/**
 * Performs a credit rating using plaintext
 * 
 */
public class PlaintextCreditRater {

  /**
   * It is assumed that there is an interval for each value.
   * @param values
   * @param intervals
   * @param scores
   * @return
   */
  public static int calculateScore(int[] values, int[][] intervals, int[][] scores) {
    int score = 0;
    
    for(int i =0; i< values.length; i++) {
      score += computeInterval(values[i], intervals[i], scores[i]);
    }
    
    return score;
  }


  private static int computeInterval(int value, int[] interval, int[] scores) {
    int count = 0;
    for(int i = 0; i < interval.length; i++) {
      if(value <= interval[i]) {
        return scores[count];
      }
      count++;
    }
    
    return scores[count];
  }
  
}
