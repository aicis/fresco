package dk.alexandra.fresco.tools.bitTriples;

import dk.alexandra.fresco.tools.bitTriples.elements.MultiplicationTriple;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Test;

public class BitTripleTest extends NetworkedTest{


    private List<MultiplicationTriple> runBitTriple(BitTriplesTestContext ctx){
    BitTriple bitTriple =
        new BitTriple(
            ctx.getResourcePool(),
            ctx.getNetwork(),
            ctx.getResourcePool().getLocalSampler().getNext(ctx.getComputationalSecurityBitParameter()),
            ctx.getResourcePool().getLocalSampler().getNext(ctx.getComputationalSecurityBitParameter()));
        return  bitTriple.getTriples(1024);
    }

    private List<MultiplicationTriple> runBitTripleRealisticParameters(BitTriplesTestContext ctx){
    BitTriple bitTriple =
        new BitTriple(
            ctx.getResourcePool(),
            ctx.getNetwork(),
            ctx.getResourcePool().getLocalSampler().getNext(ctx.getComputationalSecurityBitParameter()),
            ctx.getResourcePool().getLocalSampler().getNext(ctx.getComputationalSecurityBitParameter()));
        return  bitTriple.getTriples(1024);
    }

    @Test
    public void getTriplesTwoParties() {
        initContexts(2);
        List<Callable<List<MultiplicationTriple>>> tasks = new ArrayList<>();
        tasks.add(() -> runBitTriple(contexts.get(1)));
        tasks.add(() -> runBitTriple(contexts.get(2)));

        testRuntime.runPerPartyTasks(tasks);
    }

    @Test
    public void getTriplesTwoPartiesRealisticParameters() {
        initContexts(2, new BitTripleSecurityParameters());
        List<Callable<List<MultiplicationTriple>>> tasks = new ArrayList<>();
        tasks.add(() -> runBitTriple(contexts.get(1)));
        tasks.add(() -> runBitTriple(contexts.get(2)));

        testRuntime.runPerPartyTasks(tasks);
    }

    @Test
    public void getTriplesThreeParties() {
        initContexts(3);
        List<Callable<List<MultiplicationTriple>>> tasks = new ArrayList<>();
        tasks.add(() -> runBitTriple(contexts.get(1)));
        tasks.add(() -> runBitTriple(contexts.get(2)));
        tasks.add(() -> runBitTriple(contexts.get(3)));

        testRuntime.runPerPartyTasks(tasks);
    }
}
