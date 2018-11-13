package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.arithm.Addable;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.annotation.XmlElementRef;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class testFileWriter
    extends NetworkedTest {
  private final FieldElement macKeyShareOne = new FieldElement(11231, getModulus());
  private final FieldElement macKeyShareTwo = new FieldElement(7719, getModulus());
  private final StrictBitVector jointSeed = new StrictBitVector(new byte[]{(byte) 0x42});
  private final int noParties = 2;

  private List<AuthenticatedElement> runReader(MascotTestContext ctx) {
    ElementWriter writer = new ElementWriter();
    ElementPreprocessingFile settingsFile = writer.load(ctx.getMyId() + ".dat");
    return settingsFile.getElements();
  }

  private List<AuthenticatedElement> runWriter(MascotTestContext ctx, FieldElement macKeyShare, int numElements) {
    Mascot mascot = new Mascot(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare);
    ElementPreprocessingFile settingsFile =
        new ElementPreprocessingFile(ctx.getResourcePool(), macKeyShare, jointSeed);
    List<AuthenticatedElement> elements = mascot.getRandomElements(numElements);
    settingsFile.appendElements(elements);
    ElementWriter writer = new ElementWriter();
    writer.process(ctx.getMyId() + ".dat", settingsFile);
    return elements;
  }

  @After
  public void removeFiles() {
    for (int i = 1; i <= noParties; i++) {
      File file = new File(i + ".dat");
      file.delete();
      file = new File(i + ".dat.cont");
      file.delete();
    }
  }

  @Test
  public void testFileWrite() {
    int amountOfElements = 1024;
    // set up runtime environment and get contexts
    initContexts(noParties);

    // define per party task with params
    List<Callable<List<AuthenticatedElement>>> writingTasks = new ArrayList<>();
    writingTasks.add(() -> runWriter(contexts.get(1), macKeyShareOne, amountOfElements));
    writingTasks.add(() -> runWriter(contexts.get(2), macKeyShareTwo, amountOfElements));

    List<List<AuthenticatedElement>> writtenResults = testRuntime.runPerPartyTasks(writingTasks);

    // Read the file
    List<Callable<List<AuthenticatedElement>>> readingTasks = new ArrayList<>();
    readingTasks.add(() -> runReader(contexts.get(1)));
    readingTasks.add(() -> runReader(contexts.get(2)));
    List<List<AuthenticatedElement>> readResults = testRuntime.runPerPartyTasks(readingTasks);

    for (int p = 0; p < 2; p++) {
      assertEquals(readResults.get(p).size(), amountOfElements);
      for (int i = 0; i < amountOfElements; i++) {
        AuthenticatedElement writtenElem = writtenResults.get(p).get(i);
        AuthenticatedElement readElem = readResults.get(p).get(i);
        assertEquals(writtenElem.getModulus(), readElem.getModulus());
        assertEquals(writtenElem.getMac().getValue(), readElem.getMac().getValue());
        assertEquals(writtenElem.getMac().getBitLength(), readElem.getMac().getBitLength());
        assertEquals(writtenElem.getShare().getValue(), readElem.getShare().getValue());
        assertEquals(writtenElem.getShare().getBitLength(), readElem.getShare().getBitLength());
      }
    }

  }

}
