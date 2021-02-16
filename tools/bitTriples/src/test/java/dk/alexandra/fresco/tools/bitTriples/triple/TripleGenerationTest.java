package dk.alexandra.fresco.tools.bitTriples.triple;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.BitTriplesTestContext;
import dk.alexandra.fresco.tools.bitTriples.NetworkedTest;
import dk.alexandra.fresco.tools.bitTriples.field.MultiplicationTriple;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrgImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Test;

public class TripleGenerationTest extends NetworkedTest {

    private List<MultiplicationTriple> runTripleGeneration(BitTriplesTestContext ctx){
        BytePrgImpl jointSampler = new BytePrgImpl(new StrictBitVector(ctx.getPrgSeedLength()));
        TripleGeneration tripleGeneration =
            new TripleGeneration(ctx.getResourcePool(),ctx.getNetwork(),16, jointSampler);
        return  tripleGeneration.triple(8);
    }

    @Test
    public void triple() {
        initContexts(2);
        List<Callable<List<MultiplicationTriple>>> tasks = new ArrayList<>();
        tasks.add(() -> runTripleGeneration(contexts.get(1)));
        tasks.add(() -> runTripleGeneration(contexts.get(2)));

        testRuntime.runPerPartyTasks(tasks);
    }
}
