package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultBenchmark {
  private static final int MULTIPLCIATIONS = 20000000;
  private static final int ITERATIONS = 3;
  private static final int WARMUP = 5;

  public static void main(String[] args) {
    MultBenchmark bench = new MultBenchmark();
    System.out.println("Executing " + MULTIPLCIATIONS + " multiplications, " + ITERATIONS + " times each.");
    double res = bench.runCompUint128();
    System.out.println("CompUint128 average is " + res);
//    res = bench.runBigIntModulus();
//    System.out.println("BigInteger WITH modulus reduction average is " + res);
    res = bench.runBigInt();
    System.out.println("BigInteger WITHOUT modulus reduction average is " + res);
    // TODO does not seem to scale linearely with amount of elements, try regenrating all random elements, Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
  }

  private double runCompUint128() {
    Random rand = new Random();
    byte[] bytes = new byte[128 / 8];
    List<CompUInt128> left, right, out;
    List<Long> times = new ArrayList<>(ITERATIONS);
    left = new ArrayList<>(MULTIPLCIATIONS);
    right = new ArrayList<>(MULTIPLCIATIONS);
    for (int j = 0; j < MULTIPLCIATIONS; j++) {
      rand.nextBytes(bytes);
      left.add(new CompUInt128(bytes));
      rand.nextBytes(bytes);
      right.add(new CompUInt128(bytes));
    }
    for (int i = 0; i < ITERATIONS + WARMUP; i++) {
      out = new ArrayList<>(MULTIPLCIATIONS);
      long startTime = System.currentTimeMillis();
      for (int j = 0; j < MULTIPLCIATIONS; j++) {
        out.add(left.get(j).multiply(right.get(j)));
      }
      long endTime = System.currentTimeMillis();
      // Print a random value to make sure the execution does not get optimized away
      int index = rand.nextInt(MULTIPLCIATIONS);
      System.out.println(out.get(index));
      if (i >= WARMUP) {
        times.add(endTime - startTime);
      }
    }
    return ((double) times.stream().mapToInt(t -> t.intValue()).sum())/ITERATIONS;
  }

  private double runBigInt() {
    Random rand = new Random();
    byte[] bytes = new byte[128 / 8];
    List<BigInteger> left, right, out;
    List<Long> times = new ArrayList<>(ITERATIONS);
    left = new ArrayList<>(MULTIPLCIATIONS);
    right = new ArrayList<>(MULTIPLCIATIONS);
    for (int j = 0; j < MULTIPLCIATIONS; j++) {
      rand.nextBytes(bytes);
      left.add(new BigInteger(bytes));
      rand.nextBytes(bytes);
      right.add(new BigInteger(bytes));
    }
    for (int i = 0; i < ITERATIONS + WARMUP; i++) {
      out = new ArrayList<>(MULTIPLCIATIONS);
      long startTime = System.currentTimeMillis();
      for (int j = 0; j < MULTIPLCIATIONS; j++) {
        out.add(left.get(j).multiply(right.get(j)));
      }
      long endTime = System.currentTimeMillis();
      // Print a random value to make sure the execution does not get optimized away
      int index = rand.nextInt(MULTIPLCIATIONS);
      System.out.println(out.get(index));
      if (i >= WARMUP) {
        times.add(endTime - startTime);
      }
    }
    return ((double) times.stream().mapToInt(t -> t.intValue()).sum())/ITERATIONS;
  }

  private double runBigIntModulus() {
    Random rand = new Random();
    byte[] bytes = new byte[128 / 8];
    List<BigInteger> left, right, out;
    List<Long> times = new ArrayList<>(ITERATIONS);
    left = new ArrayList<>(MULTIPLCIATIONS);
    right = new ArrayList<>(MULTIPLCIATIONS);
    for (int j = 0; j < MULTIPLCIATIONS; j++) {
      rand.nextBytes(bytes);
      left.add(new BigInteger(bytes));
      rand.nextBytes(bytes);
      right.add(new BigInteger(bytes));
    }
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    for (int i = 0; i < ITERATIONS + WARMUP; i++) {
      out = new ArrayList<>(MULTIPLCIATIONS);

      long startTime = System.currentTimeMillis();
      for (int j = 0; j < MULTIPLCIATIONS; j++) {
        out.add(left.get(j).multiply(right.get(j)).mod(modulus));
      }
      long endTime = System.currentTimeMillis();
      // Print a random value to make sure the execution does not get optimized away
      int index = rand.nextInt(MULTIPLCIATIONS);
      System.out.println(out.get(index));
      if (i >= WARMUP) {
        times.add(endTime - startTime);
      }
    }
    return ((double) times.stream().mapToInt(t -> t.intValue()).sum())/ITERATIONS;
  }
}



