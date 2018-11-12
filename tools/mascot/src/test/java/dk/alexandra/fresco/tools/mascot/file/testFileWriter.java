package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.arithm.Addable;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import org.junit.Test;

import javax.xml.bind.annotation.XmlElementRef;
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

  private List<AuthenticatedElement> runWriter(MascotTestContext ctx, FieldElement macKeyShare, int numElements) {
    Mascot mascot = new Mascot(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare);
    ElementPreprocessingFile settingsFile =
        new ElementPreprocessingFile(ctx.getResourcePool(), macKeyShare, jointSeed);
    List<AuthenticatedElement> elements = mascot.getRandomElements(numElements);
    settingsFile.appendElements(elements);
    ElementWriter writer = new ElementWriter();
    writer.process("test" + ctx.getMyId() + ".dat", settingsFile);
    return elements;
  }

  @Test
  public void testFileWrite() {
    // set up runtime environment and get contexts
    initContexts(2);

    // define per party task with params
    List<Callable<List<AuthenticatedElement>>> tasks = new ArrayList<>();
    tasks.add(() -> runWriter(contexts.get(1), macKeyShareOne, 1024));
    tasks.add(() -> runWriter(contexts.get(2), macKeyShareTwo, 1024));

    List<List<AuthenticatedElement>> writtenResults = testRuntime.runPerPartyTasks(tasks);
    // Check the written values are the same as read values
  }

}
