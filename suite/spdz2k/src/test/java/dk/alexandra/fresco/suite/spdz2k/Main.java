package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt64;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
  private static final int MULTIPLICATIONS = 1000000;
  private static final int ITERATIONS = 100;
  private static final int WARMUP = 5;

  public static void main(String[] args) {
    int size = 128;
    System.out.println("Executing " + MULTIPLICATIONS + " multiplications, " + ITERATIONS + " times each.");
    List<Long> times;
    times = runBigIntModulus(size);
    System.out.println("BigInteger WITH modulus reduction, " + size + " average is " + mean(times) + ", standard deviation is " + std(times));
    times = runBigInt(size);
    System.out.println("BigInteger WITHOUT modulus reduction, " + size + " average is " + mean(times) + ", standard deviation is " + std(times));
    times = runCompUint128();
    System.out.println("ComputeUInt128 average is " + mean(times) + ", standard deviation is " + std(times));
    size = 64;
    times = runBigIntModulus(size);
    System.out.println("BigInteger WITH modulus reduction, " + size + " average is " + mean(times) + ", standard deviation is " + std(times));
    times = runBigInt(size);
    System.out.println("BigInteger WITHOUT modulus reduction, " + size + " average is " + mean(times) + ", standard deviation is " + std(times));
    times = runCompUint64();
    System.out.println("ComputeUInt64 average is " + mean(times) + ", standard deviation is " + std(times));
  }

  private static double mean(List<Long> times) {
    return ((double) times.stream().mapToInt(t -> t.intValue()).sum())/ITERATIONS;
  }

  private static double std(List<Long> times ) {
    double mean = mean(times);
    double temp = 0.0;
    for (long current : times) {
      temp += (((double)current) - mean)*(((double)current) - mean);
    }
    return Math.sqrt(temp/((double)(ITERATIONS - 1)));
  }

  private static List<Long> runCompUint128() {
    Random rand = new Random();
    byte[] bytes = new byte[128 / 8];
    List<CompUInt128> left, right, out;
    List<Long> times = new ArrayList<>(ITERATIONS);
    for (int i = 0; i < ITERATIONS + WARMUP; i++) {
      left = new ArrayList<>(MULTIPLICATIONS);
      right = new ArrayList<>(MULTIPLICATIONS);
      out = new ArrayList<>(MULTIPLICATIONS);
      for (int j = 0; j < MULTIPLICATIONS; j++) {
        rand.nextBytes(bytes);
        left.add(new CompUInt128(bytes));
        right.add(new CompUInt128(bytes));
      }
      long startTime = System.currentTimeMillis();
      for (int j = 0; j < MULTIPLICATIONS; j++) {
        out.add(left.get(j).multiply(right.get(j)));
      }
      long endTime = System.currentTimeMillis();
      // Print a random value to make sure the execution does not get optimized away
      int index = rand.nextInt(MULTIPLICATIONS);
      System.out.println(out.get(index));
      if (i >= WARMUP) {
        times.add(endTime - startTime);
      }
    }
    System.out.println(times);
    return times;
  }

  private static List<Long> runCompUint64() {
    Random rand = new Random();
    byte[] bytes = new byte[128 / 8];
    List<CompUInt64> left, right, out;
    List<Long> times = new ArrayList<>(ITERATIONS);
    for (int i = 0; i < ITERATIONS + WARMUP; i++) {
      left = new ArrayList<>(MULTIPLICATIONS);
      right = new ArrayList<>(MULTIPLICATIONS);
      out = new ArrayList<>(MULTIPLICATIONS);
      for (int j = 0; j < MULTIPLICATIONS; j++) {
        rand.nextBytes(bytes);
        left.add(new CompUInt64(bytes));
        right.add(new CompUInt64(bytes));
      }
      long startTime = System.currentTimeMillis();
      for (int j = 0; j < MULTIPLICATIONS; j++) {
        out.add(left.get(j).multiply(right.get(j)));
      }
      long endTime = System.currentTimeMillis();
      // Print a random value to make sure the execution does not get optimized away
      int index = rand.nextInt(MULTIPLICATIONS);
      System.out.println(out.get(index));
      if (i >= WARMUP) {
        times.add(endTime - startTime);
      }
    }
    System.out.println(times);
    return times;
  }

  private static List<Long> runBigInt(int size) {
    Random rand = new Random();
    byte[] bytes = new byte[size / 8];
    List<BigInteger> left, right, out;
    List<Long> times = new ArrayList<>(ITERATIONS);
    for (int i = 0; i < ITERATIONS + WARMUP; i++) {
      left = new ArrayList<>(MULTIPLICATIONS);
      right = new ArrayList<>(MULTIPLICATIONS);
      out = new ArrayList<>(MULTIPLICATIONS);
      for (int j = 0; j < MULTIPLICATIONS; j++) {
        rand.nextBytes(bytes);
        left.add(new BigInteger(bytes));
        right.add(new BigInteger(bytes));
      }
      long startTime = System.currentTimeMillis();
      for (int j = 0; j < MULTIPLICATIONS; j++) {
        out.add(left.get(j).multiply(right.get(j)));
      }
      long endTime = System.currentTimeMillis();
      // Print a random value to make sure the execution does not get optimized away
      int index = rand.nextInt(MULTIPLICATIONS);
      System.out.println(out.get(index));
      if (i >= WARMUP) {
        times.add(endTime - startTime);
      }
    }
    System.out.println(times);
    return times;
  }

  private static List<Long> runBigIntModulus(int size) {
    Random rand = new Random();
    byte[] bytes = new byte[size / 8];
    List<BigInteger> left, right, out;
    List<Long> times = new ArrayList<>(ITERATIONS);
    BigInteger modulus = ModulusFinder.findSuitableModulus(size);
    for (int i = 0; i < ITERATIONS + WARMUP; i++) {
      left = new ArrayList<>(MULTIPLICATIONS);
      right = new ArrayList<>(MULTIPLICATIONS);
      out = new ArrayList<>(MULTIPLICATIONS);
      for (int j = 0; j < MULTIPLICATIONS; j++) {
        rand.nextBytes(bytes);
        left.add(new BigInteger(bytes));
        right.add(new BigInteger(bytes));
      }
      long startTime = System.currentTimeMillis();
      for (int j = 0; j < MULTIPLICATIONS; j++) {
        out.add(left.get(j).multiply(right.get(j)).mod(modulus));
      }
      long endTime = System.currentTimeMillis();
      // Print a random value to make sure the execution does not get optimized away
      int index = rand.nextInt(MULTIPLICATIONS);
      System.out.println(out.get(index));
      if (i >= WARMUP) {
        times.add(endTime - startTime);
      }
    }
    System.out.println(times);
    return times;
  }
}



