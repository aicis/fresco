package dk.alexandra.fresco.tools.mascot;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.junit.Test;

public class TestNetworkedMascotResourcePoolImpl extends NetworkedTest {

  private RotBatch createRotTask(MascotTestContext ctx, int otherId) {
    Map<Integer, RotList> seedOtsMap = new HashMap<>();
    Ot ot = new DummyOt(otherId, ctx.getNetwork());
    RotList seedOts = new RotList(new AesCtrDrbg(new byte[32]), 8);
    if (ctx.getMyId() < otherId) {
      seedOts.send(ot);
      seedOts.receive(ot);
    } else {
      seedOts.receive(ot);
      seedOts.send(ot);
    }
    seedOtsMap.put(otherId, seedOts);
    MascotResourcePool resourcePool = new MascotResourcePoolImpl(ctx.getMyId(),
        ctx.getNoOfParties(),
        1, new AesCtrDrbg(new byte[32]), seedOtsMap, new MascotSecurityParameters(8, 8, 8, 3));
    return resourcePool.createRot(otherId, ctx.getNetwork());
  }

  @Test
  public void testCreateRot() {
    initContexts(2);

    Callable<RotBatch> partyOneTask = () -> createRotTask(contexts.get(1), 2);
    Callable<RotBatch> partyTwoTask = () -> createRotTask(contexts.get(2), 1);

    List<RotBatch> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
    assertTrue(results.get(0) != null);
    assertTrue(results.get(1) != null);
  }

}
