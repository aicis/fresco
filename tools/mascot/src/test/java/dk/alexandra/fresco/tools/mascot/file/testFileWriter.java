package dk.alexandra.fresco.tools.mascot.file;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.Test;

public class testFileWriter
    extends NetworkedTest {

  private final FieldElement macKeyShareOne = new FieldElement(11231, getModulus());
  private final FieldElement macKeyShareTwo = new FieldElement(7719, getModulus());
  private final int noParties = 2;
  private final int amountOfElements = 1024;
  private final int iterations = 5;

  @After
  public void removeFiles() {
    for (int i = 1; i <= noParties; i++) {
      File file = new File(i + ".set");
      file.delete();
      file = new File(i + ".cont");
      file.delete();
    }
  }

  private List<AuthenticatedElement> runReader(int id) throws IOException {
    SettingsIO<MascotSettings> settingsIO = new SettingsIO();
    MascotSettings settings = settingsIO.readFile(id + ".set");
    AuthenticatedElementSerializer serializer = new AuthenticatedElementSerializer(
        settings.getModulus());
    ElementIO<AuthenticatedElement> writer = new ElementIO<>(id + ".cont", serializer);
    List<AuthenticatedElement> allElements = new LinkedList<>();
    for (int i = 0; i < iterations; i++) {
      List<AuthenticatedElement> loadedElements = writer.readData(amountOfElements);
      allElements.addAll(loadedElements);
    }
    writer.close();
    return allElements;
  }

  private List<AuthenticatedElement> runWriter(MascotTestContext ctx, FieldElement macKeyShare)
      throws IOException {
    Mascot mascot = new Mascot(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare);
    MascotSettings settings =
        new MascotSettings(ctx.getNoOfParties(), ctx.getModulus(), macKeyShare);
    SettingsIO<MascotSettings> settingsIO = new SettingsIO();
    settingsIO.writeFile(ctx.getMyId() + ".set", settings);
    AuthenticatedElementSerializer serializer = new AuthenticatedElementSerializer(
        ctx.getModulus());
    ElementIO<AuthenticatedElement> writer = new ElementIO<>(ctx.getMyId() + ".cont",
        serializer);
    List<AuthenticatedElement> allElements = new LinkedList<>();
    for (int i = 0; i < iterations; i++) {
      List<AuthenticatedElement> elements = mascot.getRandomElements(amountOfElements);
      writer.writeData(elements);
      allElements.addAll(elements);
    }
    writer.close();
    return allElements;
  }


  @Test
  public void testAuthElem() {
    // set up runtime environment and get contexts
    initContexts(noParties);

    // define per party task with params
    List<Callable<List<AuthenticatedElement>>> writingTasks = new ArrayList<>();
    writingTasks.add(() -> runWriter(contexts.get(1), macKeyShareOne));
    writingTasks.add(() -> runWriter(contexts.get(2), macKeyShareTwo));

    List<List<AuthenticatedElement>> writtenResults = testRuntime.runPerPartyTasks(writingTasks);

    // Read the file
    List<Callable<List<AuthenticatedElement>>> readingTasks = new ArrayList<>();
    readingTasks.add(() -> runReader(1));
    readingTasks.add(() -> runReader(2));
    List<List<AuthenticatedElement>> readResults = testRuntime.runPerPartyTasks(readingTasks);

    for (int p = 0; p < 2; p++) {
      assertEquals(readResults.get(p).size(), iterations * amountOfElements);
      for (int i = 0; i < iterations * amountOfElements; i++) {
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
