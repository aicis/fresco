package dk.alexandra.fresco.suite.spdz.storage;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Test;

public class TestInitializeStorageCleanupVisitor {

  @Test
  public void visitFileFailedRethrowsOriginalException() {
    IOException failure = new IOException("synthetic file-visit failure");
    IOException thrown = assertThrows(
        IOException.class,
        () -> InitializeStorage.deletionVisitor().visitFileFailed(
            Paths.get("synthetic-file"), failure));

    assertSame(failure, thrown);
  }

  @Test
  public void postVisitDirectoryRethrowsOriginalException() {
    IOException failure = new IOException("synthetic directory-visit failure");
    IOException thrown = assertThrows(
        IOException.class,
        () -> InitializeStorage.deletionVisitor().postVisitDirectory(
            Paths.get("synthetic-dir"), failure));

    assertSame(failure, thrown);
  }
}
