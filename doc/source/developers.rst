.. _developers:

For Developers
==============

In this section we give some tips and guidelines for developing and contributing code to FRESCO.

Directory Structure
-------------------

The FRESCO root directory (see https://github.com/aicis/fresco) contains a number
sub-directories containing multiple sub-projects for FRESCO. Here we describe the most important
directories:

* `core <https://github.com/aicis/fresco/tree/master/core>`_ - contains the core FRESCO
  framework including the application interface, library of generic functionality and dummy suites.

* `demos <https://github.com/aicis/fresco/tree/master/demos>`_ - contains a number of demos
  demonstrating how FRESCO can be used. Each demo has its own sub-project.

* `doc <https://github.com/aicis/fresco/tree/master/doc>`_ - contains the source of the documentation for this site.

* `suite <https://github.com/aicis/fresco/tree/master/suite>`_ - contains the protocol suites implemented in FRESCO each as its own sub-project.

* `tools <https://github.com/aicis/fresco/tree/master/tools>`_ - contains various tools used in FRESCO each as its own sub-project.

We use `Maven <https://maven.apache.org/>`_ to manage FRESCO, and within each sub-project we use
the standard Maven directory structure.

Developing using an IDE
-----------------------

The FRESCO team (mostly) uses `Eclipse <https://www.eclipse.org/>`_ to develop FRESCO. To develop
using Eclipse, first check out a fork of FRESCO from GitHub. Then to import FRESCO into Eclipse
choose ::

  File > Import... > Maven > Existing Maven Projects

and select the FRESCO root directory.

To help conform to the code style used in FRESCO, as described in the `Code Style`_ section, we
recommend installing the Checkstyle plugin for Eclipse and configuring it to use the Google style.
This plugin can also be used to generate a code formatter for Eclipse. To ensure imports are ordered
correctly, you may also need to go to ::

  Eclipse > Preferences... > Java > Code Style > Organize Imports

and delete all groups in the list displayed (as the Google style dictates that all imports
must be in a single block).

To help fulfill the code coverage goal described in `Testing`_ we also recommend installing the
Jacoco plugin. This eases checking code coverage on your changes locally.

Alternatively some IntelliJ support is also present - look for the .idea files in the root of the
repository.

.. _`Code Style`: 

Code Style
----------

To keep the code style consistent we use the style defined by `Google
<https://google.github.io/styleguide/javaguide.html>`_. We prefer to keep the code from generating
compile warnings, using the ``@SuppressWarnings`` annotation sparingly in case of unavoidable warnings.

.. _`Testing`:

Testing
-------

We use `JUnit4 <http://junit.org/junit4/>`_ for testing. We use the `Travis
<https://travis-ci.org/aicis/fresco>`_ tool to continuously check that all code committed to the
repository compiles and passes all tests. We strive for 100% test coverage of the FRESCO code and
use the `Codecov <https://codecov.io/gh/aicis/fresco>`_ tool to automatically check that new
patches have 100% coverage and do not decrease the overall test coverage.

For each sub-project tests are located in the source code folder named ``test`` separated from the
main code, as per the standard Maven directory structure. When writing tests for something in
package ``x.y.z`` the test should belong to the same package. This way, methods that are
*package private* and therefore not exposed in the FRESCO API can also be tested.

We work with two classes of tests:

* Regular tests. These should be fast and not rely on any external dependencies such as a server
  already running. I.e., it should always possible to check out the code and just run these tests
  with only meeting the requirements seen in the :ref:`install<install>` section.

* Integration tests. These are tests that for example rely on external databases being set up, or
  involve deployment to different hosts. You can mark a test class or test method as integration
  test by using the ``@Category`` annotation like so:

  .. sourcecode:: java

    @Test @Category(IntegrationTest.class) public void testSomething() { // Your test goes here. }

Integration tests are ignored when you the FRESCO tests suite using the Maven command ::

  mvn test

but are included when you run ::

  mvn integration-test


A few good practices regarding tests:

#. Write tests.

#. Don't delete, comment, or ``@Ignore`` tests unless you really know what you are doing.

#. Make sure that tests are independent of each other.

#. Tests should be deterministic. Use a pseudo-random generator with a fixed seed if you need
   randomness.

#. Working tests should be silent when they work. Use log level ``Level.FINE`` if needed.


Writing Tests for a Protocol Suite
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you are developing a new protocol suite you may want to write tests in the same way as the tests
for suites that are already included in FRESCO. Consider, e.g., the SPDZ suite. A helper method is
made:

.. sourcecode:: java

  protected void runTest(TestThreadRunner.TestThreadFactory f, EvaluationStrategy evalStrategy,
	NetworkingStrategy network, PreprocessingStrategy preProStrat, int noOfParties) throws Exception

The first argument to ``runTest`` is a ``TestThreadFactory`` which defines which logic should be
tested. It is a factory that provides threads for each party in the test. If the protocol to test is
symmetric, each thread is identical. The test framework makes sure that each thread has access to
its own ``partyId`` so if the test requires the parties to do different things, they can branch on
their partyId.

The rest of the arguments to ``runTest`` are parameters over which you want your tests to vary. For
example this could be the number of players and evaluation strategy. But it can also include parameters
specific to your suite. The ``runTest`` should set up the remaining parameters for your test --
those parameters that should remain fixed in all your tests.

Then create a number of small tests, like the following:

.. sourcecode:: java

   @Test
   public void test_MultAndAdd_Sequential() throws Exception {
     runTest(new BasicArithmeticTests.TestSimpleMultAndAdd(), EvaluationStrategy.SEQUENTIAL,
       NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
     }
   
It is fine to let the name reflect the specific parameters used in the test. Note how we use a
generic test here: The test ``BasicArithmeticTests.TestSimpleMultAndAdd`` can be used to test
multiplications and additions for any protocol suite that supports basic arithmetic operations, so
there is no need to rewrite such tests. Only write your own specific tests if you need to test some
specific functionality of your suite that no other suite has, otherwise consider making the test
generic such that it can be reused by others.

Writing many small tests like this makes it easy to decide later which of the tests to include. The
"unit" test suite should be relatively quick and not require external setup. If it depends on such
things, mark it with ``@Category(IntegrationTest.class)``.


.. _documentation:

Building the Documentation
--------------------------

The documentation will be built automatically and uploaded to `fresco.readthedocs.org
<http://fresco.readthedocs.org>`_ when new changes are pushed to the repository. Before committing
changes to the documentation, it is a good idea to build the documentation locally and check that it
looks ok. This can be done as follows.

Building the docs requires Sphinx to be installed. A good way to do this is by using *virtualenv*.
Using virtualenv installs Sphinx in a local folder that can be easily removed, and it ensures that
the installation does not have any side effects: Go to the ``doc`` folder. Then create a new virtual
environment: ::

  virtualenv env
  source ./env/bin/activate
  pip install -r requirements.txt

If the install fails, you might have to update pip. Just follow the directions pip gives you. This
only needs to be done once. When done, you can activate the virtual environment just by doing::

  source ./env/bin/activate

Once activated, you can build documentation with: ::

  make html

On Mac OS X you may need to set the following environment variables: ::

  export LC_ALL=en_US.UTF-8
  export LANG=en_US.UTF-8

You can enter the two lines directly in your terminal or to add them to your ``~/.bash_profile``.

Once built, you can view the result, open the file ``doc/build/html/index.hmtl`` with a web browser.
