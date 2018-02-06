.. _install:

Installation
============

FRESCO is designed to run on Linux, MacOS, and Windows. The following installation guide is tested
on Linux and MacOS.

Building FRESCO from Source
---------------------------

The prefered way to install FRESCO is by building it from the latest source. To do this, make sure
you have installed `git <http://git-scm.org>`_, `Java 8 <http://java.com>`_, and `Maven
<https://maven.apache.org/>`_.

Then in a terminal run: ::

  git clone https://github.com/aicis/fresco.git
  cd fresco
  mvn install

This will download the FRESCO soure code and dependencies, compile FRESCO, and run the test
suite. On a successful build Maven should install FRESCO on your system and a FRESCO JAR file can
now be found in the ``./target`` directory as well as in your local maven repository. Note that 
``mvn install`` will execute the test suite included with FRESCO as part of the build. To skip the tests
and only run the build, use ``mvn install -DskipTests``.

 
Downloading a Pre-Built FRESCO Release
--------------------------------------

Altenatively you can download the latest release of FRESCO from GitHub_.

.. _GitHub: https://github.com/aicis/fresco/releases
