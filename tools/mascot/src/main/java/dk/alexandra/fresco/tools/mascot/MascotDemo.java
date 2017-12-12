package dk.alexandra.fresco.tools.mascot;

import java.util.Arrays;
import java.util.List;

public class MascotDemo {

  Mascot mascot;
  
  public MascotDemo(Integer myId, List<Integer> partyIds) {
    mascot = new Mascot(myId, partyIds);
    mascot.initialize();
  }
  
  public void run() {
    System.out.println(mascot.getTriples(10));
  }
  
  public static void main(String[] args) {
    Integer myId = Integer.parseInt(args[0]);
    List<Integer> partyIds = Arrays.asList(1, 2);
    new MascotDemo(myId, partyIds).run();
  }
  
}
