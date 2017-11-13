package dk.alexandra.fresco;

import dk.alexandra.fresco.suite.tinytables.TestTinyTables;
import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.ExcludeCategory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
    //Tinytables protocol suite
    TestTinyTables.class
})
@ExcludeCategory(IntegrationTest.class)
public class TestSuite {
  //nothing
}