.. _install:

Installation
============

FRESCO is designed to run on Linux, MacOS, and Windows. The following installation guide is tested
on Linux and MacOS.

Building FRESCO from Source
---------------------------

The preferred way to install FRESCO is by building it from the latest source from GitHub. To do
this, make sure you have installed `git <http://git-scm.org>`_, `Java 8 <http://java.com>`_, and
`Maven <https://maven.apache.org/>`_.

Then in a terminal run: ::

  git clone https://github.com/aicis/fresco.git
  cd fresco
  mvn install

This will download the FRESCO source code and dependencies, compile all the FRESCO modules, and run
the test suite. On a successful build Maven should install the FRESCO modules on your system and a JAR file can now be found in the ``./target`` directory of each corresponding module, as well
as in your local Maven repository. Note, that the test suite executed on ``mvn install`` can take
several minutes. To skip the tests and only run the build, use ``mvn install -DskipTests``.

If you use Maven for your project you can then use a FRESCO module by adding it as a dependency in
your projects POM file. E.g., to use the ``core`` module add the dependency

.. sourcecode:: xml

  <dependency>
    <groupId>dk.alexandra.fresco</groupId>
    <artifactId>core</artifactId>
    <version>1.0.1-SNAPSHOT</version>
  </dependency>

possibly incrementing the version number to the current version. Note that in order to use one of the :ref:`protocol suites <protocol_suites>` in your project, you will need to add it as a dependency as well. For instance, if you want to use the SPDZ protocol suite, your POM file will need to include:

.. sourcecode:: xml

  <dependency>
    <groupId>dk.alexandra.fresco</groupId>
    <artifactId>spdz</artifactId>
    <version>1.0.1-SNAPSHOT</version>
  </dependency>

Using the Latest Release
------------------------

If you prefer to install the a released version of FRESCO you can get the source from the release
site https://github.com/aicis/fresco/releases, and run ``mvn install`` as described above.

Alternatively If your project uses Maven you could just add the dependency to your projects POM file
and have Maven download the dependency from the Central Repository. E.g., to use a release version
of the ``core`` and ``spdz`` modules add the dependencies

.. sourcecode:: xml

  <dependency>
    <groupId>dk.alexandra.fresco</groupId>
    <artifactId>core</artifactId>
    <version>1.0.0</version>
  </dependency>

  <dependency>
    <groupId>dk.alexandra.fresco</groupId>
    <artifactId>spdz</artifactId>
    <version>1.0.0</version>
  </dependency>

possibly adjusting the version tag to the desired version.
