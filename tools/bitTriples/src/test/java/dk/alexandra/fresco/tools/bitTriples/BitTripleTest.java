package dk.alexandra.fresco.tools.bitTriples;

import dk.alexandra.fresco.tools.bitTriples.elements.MultiplicationTriple;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Assert;
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

    @Test
    public void testTriplesToCreate() {
        Assert.assertEquals(163,BitTriple.triplesToCreate(50,40));
        Assert.assertEquals(2047,BitTriple.triplesToCreate(200,40));
        Assert.assertEquals(16389,BitTriple.triplesToCreate(3000,40));
        Assert.assertEquals(233016,BitTriple.triplesToCreate(18000,40));
        Assert.assertEquals(1864191,BitTriple.triplesToCreate(400000,40));

        Assert.assertEquals(63,BitTriple.triplesToCreate(50,64));
        Assert.assertEquals(511,BitTriple.triplesToCreate(200,64));
        Assert.assertEquals(7281,BitTriple.triplesToCreate(3000,64));
        Assert.assertEquals(83885,BitTriple.triplesToCreate(18000,64));
        Assert.assertEquals(671088,BitTriple.triplesToCreate(400000,64));
        Assert.assertEquals(5368708,BitTriple.triplesToCreate(1000000,64));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTriplesToCreateTooLarge40(){
      BitTriple.triplesToCreate(1864192,40);
  }

    @Test(expected = IllegalArgumentException.class)
    public void testTriplesToCreateTooLarge64(){
      BitTriple.triplesToCreate(5368709,64);
  }
}
