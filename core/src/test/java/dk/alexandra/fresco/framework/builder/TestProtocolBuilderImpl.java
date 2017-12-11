package dk.alexandra.fresco.framework.builder;

import org.junit.Test;

public class TestProtocolBuilderImpl {

  @Test(expected = IllegalStateException.class)
  public void build() throws Exception {
    ProtocolBuilderImpl<BuilderTestClass> protocolBuilder = new BuilderTestClass();
    protocolBuilder.append(null);
    protocolBuilder.build();
    protocolBuilder.append(null);
  }

  private static class BuilderTestClass extends ProtocolBuilderImpl<BuilderTestClass> {

    public BuilderTestClass() {
      super(null, false);
    }
  }
}