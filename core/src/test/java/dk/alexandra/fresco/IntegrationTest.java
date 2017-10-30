package dk.alexandra.fresco;

/**
 * Use this interface in combination with @Category to specify test classes and
 * methods that are part of integration tests, i.e., that are not run with 'mvn
 * test', but with 'mvn integration-test' and 'mvn verify'.
 *
 * See
 * http://www.javacodegeeks.com/2015/01/separating-integration-tests-from-unit-
 * tests-using-maven-failsafe-junit-category.html
 * 
 */
public interface IntegrationTest {

}
