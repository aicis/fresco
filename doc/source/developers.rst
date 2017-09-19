
For Developers
==============

.. _contributing:

Contributing
------------

We will be happy to include well-written code that is compliant with
the overall FRESCO design. This could be bug fixes, new protocol
suites, or applications that are generic enough to fit into the FRESCO
standard library. It could also be improvement to this documentation.
You are welcome to use the issue tracker at `GitHub
<https://github.com/aicis/fresco/issues>`_ or email us at
fresco@alexandra.dk with any ideas, etc.

The team of people that are or have been involved in developing FRESCO
is (ordered alphabetically):

* Emil Bremer Orloff <ebo@partisia.dk>
* Jonas Lindstrøm <jonas.lindstrom@alexandra.dk>
* Kasper Damgaard <kasper.damgaard@alexandra.dk>
* Michael Bladt Stausholm <michael.stausholm@alexandra.dk>
* Thomas P. Jakobsen <tpj@sepior.dk>
* Peter F. Frandsen <pff@partisia.dk>
* Peter S. Nordholt <peter.s.nordholt@alexandra.dk>
* Tomas Toft <ttoft@cs.au.dk>

If you think that your name belongs to this list, feel free to send an
email to fresco@alexandra.dk.


Pull Requests
~~~~~~~~~~~~~

The easiest way to contribute is to send us a pull request on GitHub:

* Create a fork of the FRESCO master repository at `GitHub
  <http://github.com/aicis/fresco>`_.

* Make your changes in your forked branch.

* If you write new code, be sure to add tests and update the
  documentation (in the ``doc`` folder) where appropriate.

* Add yourself to the list of contributors (at
  ``doc/source/developrs.rst``).

* Ensure the test suite passes, i.e., that ``mvn verify`` complete
  without errors.

* If you changed the documentation, build it locally and make sure it looks
  right.

* Hit the *Pull Request* button at GitHub.


Developing with Eclipse
-----------------------

To develop using Eclipse, first check out a fork of FRESCO from GitHub. Then
go to the local directory where FRESCO is located and run: ::

    $ mvn eclipse:eclipse

Then choose *File > Import ... > General > Existing Projects into Workspace*
from Eclipse and select the folder containing the FRESCO source.

IntelliJ support is also present - look for the .idea files in the root of the
git repository. 

Coding Conventions
------------------

This section contains a few guidelines for both application
developers, protocol suite developers, and developers of FRESCO
itself.

We use Google code style, so please ensure that your IDE supports this when
developing on FRESCO. The XML document describing the code style can be found in the root
of the git repository.

When writing a new piece of code, try to ensure 100% code coverage using unit
tests. We use jacoco for this purpose, and a report will be generated after
running the maven test target.

Avoid creating warnings in the code. If e.g. for some *good reason* generics
cannot be correctly parameterized, use ``@SuppressWarnings("rawtypes")`` to get
rid of the warning.


Building the Documentation
--------------------------

The documentation will be built automatically and uploaded to
`fresco.readthedocs.org <http://fresco.readthedocs.org>`_ when new changes are
pushed to the repository. Before committing changes to the documentation, it is
a good idea to build the documentation locally and check that it looks ok. This
can be done as follows.

Building the docs requires Sphinx to be installed. A good way to do this is by
using *virtualenv*. Using virtualenv installs Sphinx in a local folder that can
be easily removed, and it ensures that the installation does not have any side
effects:  Go to the ``doc`` folder. Then create a new virtual environment: ::

  $ virtualenv env
  $ source ./env/bin/activate
  $ pip install -r requirements.txt

If the install fails, you might have to update pip. Just follow the directions
pip gives you. This only needs to be done once. When done, you can activate the
virtual environment just by doing::

  $ source ./env/bin/activate

Once activated, you can build documentation with: ::

  $ make html

On Mac OS X you may need to set the following environment variables: ::

    export LC_ALL=en_US.UTF-8
    export LANG=en_US.UTF-8

You can enter the two lines directly in your terminal or to add them to your
``~/.bash_profile``.

Once built, you can view the result, open the file
``doc/build/html/index.hmtl`` with a web browser.


Testing
-------

We use JUnit4 for testing. Tests are located in the source code folder
named ``test`` in order to separate test code from the FRESCO
code. The test folder has the same package structure as the FRESCO
code itself. Whenever you write a test for something in package
``x.y.z`` the test should be put in the source folder
``/test/x/y/z``. This way, you can test methods that are not exposed
in the FRESCO API by making them *package private*.

We have two classes of tests:

* Unit tests. These should be fast and not rely on any external dependencies
  such as a server already running. Unit tests should be run continously by
  developers. Also, it should ideally be possible to check out the code and just
  run ``mvn test`` with only meeting the requirements seen in the
  :ref:`install<install>` section.

* Integration tests. This is tests that for example rely on external
  databases being set up, or involve deployment to different
  hosts. You can mark a test class or test method as integration test
  as this:

  .. sourcecode:: java

    @Test @Category(IntegrationTest.class)
    public void testSomething() {
        // Your test goes here.
    }

Integration tests are ignored when you run ::

  mvn test

but are included when you run ::

  mvn integration-test


A few good practices regarding tests:

#. Write tests.

#. Don't delete, outcomment, or ``@Ignore`` tests unless you really
   know what you are doing.

#. Make sure that tests are independent of each other.

#. Tests should be deterministic. Use a pseudo-random generator with a
   fixed seed if you need randomness.

#. Working tests should be silent when they work. Use ``Level.FINE``
   if you want a test to say something. A failing test should say a
   lot of useful things.

#. Try to obtain 100% code coverage for whatever you are testing.


Writing Tests for a Protocol Suite
----------------------------------

If you are developing a new protocol suite you should write tests in
the same way as the tests for suites that are already included in
FRESCO. Consider, e.g., the SPDZ suite. Tests are placed in the
``test`` folder under ``dk.alexandra.fresco.suite.mysuite``. A helper
method is made:

.. sourcecode:: java

   protected void runTest(TestThreadRunner.TestThreadFactory f, EvaluationStrategy evalStrategy,
	NetworkingStrategy network,
	PreprocessingStrategy preProStrat, int noOfParties) throws Exception

The first argument to ``runTest`` is a ``TestThreadFactory`` which
defines which logic should be tested. It is a factory that provides
threads for each party in the test. If the protocol to test is
symmetric, each thread is identical. The test framework makes sure
that each thread has access to its own ``partyId`` so if the test
requires the parties to do different things, they can branch on their
partyId.

The rest of the arguments to ``runTest`` are parameters over which you want your
tests to vary. For example this could be number of players and evaluation
strategy. But it can also include parameters specific to your suite. The
``runTest`` should set up the remaining parameters for your test -- those
parameters that should remain fixed in all your tests.

Then create a number of small tests, like the following:

.. sourcecode:: java

   @Test
   public void test_MultAndAdd_Sequential() throws Exception {
     runTest(new BasicArithmeticTests.TestSimpleMultAndAdd(), EvaluationStrategy.SEQUENTIAL,
       NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
     }
   
It is fine to let the name reflect the specific parameters used in the
test. Note how we use a generic test here: The test
``BasicArithmeticTests.TestSimpleMultAndAdd`` can be used to test
multiplications and additions for any protocol suite that supports
basic arithmetic operations, so there is no need to rewrite such
tests. Only write your own specific tests if you need to test some
specific functionality of your suite that no other suite has,
otherwise consider making the test generic such that it can be reused
by others.

Writing many small tests like this makes it easy to decide later which of the
tests to include. The "unit" test suite should be relatively quick and not
require external setup. If it depends on such things, mark it with
``@Category(IntegrationTest.class)``.

Versioning
----------

We use `semantic <http://semver.org>`_ versioning. To make a new
release, e.g., version 1.2.3 do:

* Update the ``pom.xml`` file to include::

    <version>1.2.3-SNAPSHOT</version>

* Update the documentation in ``doc/source/releases.rst``. Include a
  short description of new features, bug fixes, etc.

* Create a git tag in the repository. Our GitHub account is set up
  such that the new release is automatically recognized by
  `readthedocs.org <http://readthedocs.org>`_. For this to work,
  simply name the tag ``1.2.3``.

* Edit the release page on GitHub to reflect the change.
