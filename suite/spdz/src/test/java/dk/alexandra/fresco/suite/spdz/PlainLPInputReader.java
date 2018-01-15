package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.suite.spdz.utils.LinearProgrammingInputReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;

public class PlainLPInputReader implements LinearProgrammingInputReader {

  private BigInteger[][] constraintValues;
  private BigInteger[] costValues;
  private int[][] constraintPattern;
  private int[] costPattern;
  private int noVariables;
  private int noConstraints;
  private int myId;

  private boolean readInputValues;
  private boolean readInputPattern;
  private BufferedReader valuesReader;
  private BufferedReader patternReader;


  public PlainLPInputReader(BufferedReader valuesReader, BufferedReader patternReader, int myId) {
    this.valuesReader = valuesReader;
    this.patternReader = patternReader;
    this.myId = myId;
    constraintValues = null;
    costValues = null;
    constraintPattern = null;
    costPattern = null;
    readInputValues = false;
    readInputPattern = false;
    noVariables = -1;
    noConstraints = -1;
  }

  public static PlainLPInputReader getFileInputReader(File values, File pattern, int myId)
      throws FileNotFoundException {
    BufferedReader valuesReader = new BufferedReader(new FileReader(values));
    BufferedReader patternReader = new BufferedReader(new FileReader(pattern));
    return new PlainLPInputReader(valuesReader, patternReader, myId);
  }

  @Override
  public BigInteger[][] getConstraintValues() {
    if (readInputValues) {
      return constraintValues;
    } else {
      return null;
    }
  }

  @Override
  public BigInteger[] getCostValues() {
    if (readInputValues) {
      return costValues;
    } else {
      return null;
    }
  }

  @Override
  public BigInteger[] getBValues() {
    BigInteger[] B = null;
    if (readInputValues) {
      B = new BigInteger[constraintValues.length];
      for (int i = 0; i < B.length; i++) {
        BigInteger[] constraint = constraintValues[i];
        B[i] = constraint[constraint.length - 1];
      }
    }
    return B;
  }

  @Override
  public BigInteger[] getFValues() {
    BigInteger[] F = null;
    if (readInputValues) {
      F = new BigInteger[costValues.length];
      for (int i = 0; i < F.length; i++) {
        if (costValues[i] != null) {
          F[i] = costValues[i].negate();
        } else {
          F[i] = null;
        }
      }
    }
    return F;
  }

  @Override
  public BigInteger[][] getCValues() {
    BigInteger[][] C = null;
    if (readInputValues) {
      C = new BigInteger[constraintValues.length][constraintValues[0].length - 1];
      for (int i = 0; i < constraintValues.length; i++) {
        System.arraycopy(constraintValues[i], 0, C[i], 0, C[i].length);
      }
    }
    return C;
  }

  @Override
  public int[][] getConstraintPattern() {
    if (readInputPattern) {
      return constraintPattern;
    } else {
      return null;
    }
  }

  @Override
  public int[] getCostPattern() {
    if (readInputPattern) {
      return costPattern;
    } else {
      return null;
    }
  }

  @Override
  public int[] getBPattern() {
    int[] B = null;
    if (readInputPattern) {
      B = new int[noConstraints];
      for (int i = 0; i < B.length; i++) {
        int[] constraint = constraintPattern[i];
        B[i] = constraint[constraint.length - 1];
      }
    }
    return B;
  }

  @Override
  public int[] getFPattern() {
    return getCostPattern();
  }

  @Override
  public int[][] getCPattern() {
    int[][] C = null;
    if (readInputPattern) {
      C = new int[noConstraints][noVariables];
      for (int i = 0; i < noConstraints; i++) {
        System.arraycopy(constraintPattern[i], 0, C[i], 0, C[i].length);
      }
    }
    return C;
  }


  @Override
  public void readInput() throws IOException, RuntimeException {
    readPattern(patternReader);
    readValues(valuesReader);
    checkConsistency();
  }

  private void readPattern(BufferedReader patternReader) throws IOException, RuntimeException {
    if (!readInputPattern) {
      LinkedList<int[]> constraintList = new LinkedList<>();
      String line = patternReader.readLine();
      if (line != null) {
        costPattern = parsePatternLine(line);
      } else {
        throw new RuntimeException("Input pattern malformed: Empty input");
      }
      line = patternReader.readLine();
      while (line != null && !line.trim().equals("")) {
        constraintList.add(parsePatternLine(line));
        line = patternReader.readLine();
      }
      patternReader.close();

      if (noVariables < 0) {
        noVariables = costPattern.length;
      } else if (costPattern.length != noVariables) {
        throw new RuntimeException("Input malformed: input pattern and values do not match");
      }
      if (noConstraints < 0) {
        noConstraints = constraintList.size();
      } else if (constraintList.size() != noConstraints) {
        throw new RuntimeException("Input malformed: input pattern and values do not match");
      }
      if (noConstraints == 0) {
        throw new RuntimeException("Input pattern malformed: No constraints given.");
      }

      int index = 0;
      constraintPattern = new int[noConstraints][noVariables + 1];
      for (int[] row : constraintList) {
        if (row.length != noVariables + 1) {
          throw new RuntimeException("Input pattern malformed: Dimensions do not match.");
        }
        constraintPattern[index] = row;
        index++;
      }
      readInputPattern = true;
    }
  }

  private int[] parsePatternLine(String line) {
    String[] fields = line.split(",");
    int[] pattern = new int[fields.length];
    for (int i = 0; i < fields.length; i++) {
      pattern[i] = Integer.parseInt(fields[i].trim());
    }
    return pattern;
  }

  private void readValues(BufferedReader valueReader) throws IOException, RuntimeException {
    if (!readInputValues) {
      LinkedList<BigInteger[]> constraintList = new LinkedList<>();
      String line = valueReader.readLine();
      if (line != null) {
        costValues = parseValueLine(line);
      } else {
        throw new RuntimeException("Input values malformed: Empty input");
      }
      line = valueReader.readLine();
      while (line != null && !line.trim().equals("")) {
        constraintList.add(parseValueLine(line));
        line = valueReader.readLine();
      }
      valueReader.close();

      if (noVariables < 0) {
        noVariables = costValues.length;
      } else if (costValues.length != noVariables) {
        throw new RuntimeException("Input malformed: input pattern and values do not match");
      }
      if (noConstraints < 0) {
        noConstraints = constraintList.size();
      } else if (constraintList.size() != noConstraints) {
        throw new RuntimeException("Input malformed: input pattern and values do not match");
      }
      if (noConstraints == 0) {
        throw new RuntimeException("Input values malformed: No constraints given.");
      }

      int index = 0;
      constraintValues = new BigInteger[noConstraints][noVariables + 1];
      for (BigInteger[] row : constraintList) {
        if (row.length != noVariables + 1) {
          throw new RuntimeException("Input values malformed: Dimensions do not match " +
              row.length + " != " + (noVariables + 1));
        }
        constraintValues[index] = row;
        index++;
      }
      readInputValues = true;
    }
  }

  private void checkConsistency() throws RuntimeException {
    if (readInputValues && readInputPattern) {
      for (int i = 0; i < constraintValues.length; i++) {
        for (int j = 0; j < constraintValues[0].length; j++) {
          if (constraintValues[i][j] == null &&
              (constraintPattern[i][j] == myId || constraintPattern[i][j] == 0)) {
            throw new RuntimeException( 
                "Input malformed: constraint value (" + i + "," + j + ") missing");
          }
        }
      }
      for (int i = 0; i < costValues.length; i++) {
        if (costValues[i] == null && (costPattern[i] == myId || costPattern[i] == 0)) {
          throw new RuntimeException("Input malformed: cost value " + i + " missing");
        }
      }
    }
  }

  private BigInteger[] parseValueLine(String line) {
    String[] fields = line.split(",");
    BigInteger[] values = new BigInteger[fields.length];
    for (int i = 0; i < fields.length; i++) {
      try {
        values[i] = new BigInteger(fields[i].trim());
      } catch (NumberFormatException e) {
        values[i] = null;
      }
    }
    return values;
  }

  @Override
  public boolean isRead() {
    return (readInputValues && readInputPattern);
  }

  @Override
  public int getOutputId() {
    // TODO: Read this somehow, for now just output player 1
    return 1;
  }


}