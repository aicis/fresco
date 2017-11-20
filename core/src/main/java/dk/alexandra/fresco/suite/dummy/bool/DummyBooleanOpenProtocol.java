package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * Implements openings for the Dummy Boolean protocol suite, where all operations are done in the
 * clear.
 */
public class DummyBooleanOpenProtocol extends DummyBooleanNativeProtocol<Boolean>{

	private Boolean open;
  private DRes<SBool> closed;

	private int target;
	
  /**
   * Constructs a native protocol to open a closed boolean to all players.
   *
	 * @param in a computation supplying the {@link SBool} to open
	 */
	DummyBooleanOpenProtocol(DRes<SBool> in) {
	  open = null;
		closed = in;
		target = -1; // open to all
	}
	
  /**
   * Constructs a native protocol to open a closed boolean towards a spcecific player.
   * 
   * @param c a computation supplying the {@link SBool} to open
   * @param target the id of party to open towards
   */
  public DummyBooleanOpenProtocol(DRes<SBool> c, int target) {
    super();
    this.target = target;
    this.closed = c;
    this.open = null;
  }
	
	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
			Network network) {
		boolean openToAll = target == -1;
		if (resourcePool.getMyId() == target || openToAll) {
		  this.open = ((DummyBooleanSBool) this.closed.out()).getValue();
		}
		return EvaluationStatus.IS_DONE;
	}	
	
	@Override
  public Boolean out() {
    return this.open;
  }
}
