.. _install:

Installation
============

FRESCO is designed to run on Linux, Mac OS X, and Windows.


Downloading a Pre-Built FRESCO Release
--------------------------------------

You can download the latest release of FRESCO from :ref:`here
<releases>`.


Installing FRESCO from Source
-----------------------------

To install from source, make sure you have installed `git
<http://git-scm.org>`_, `Java 1.8 <http://java.com>`_, and `Maven
<https://maven.apache.org/>`_. Then run: ::

  $ git clone https://github.com/aicis/fresco/fresco.git
  $ cd fresco/core
  $ mvn install

This will download some dependencies, compile the FRESCO core, and runs our
extensive test suite. If everything works fine Maven installs FRESCO on your
system and a FRESCO JAR file can now be found in the ``./target`` folder as well
as in your local maven repository.

FRESCO is split up into several projects in order to better manage
dependencies. We have the following projects which are all included in your git
clone you just fetched:

- core
- suite
- demos
- scapiNetwork
- tools

The core project contains the :ref:`standard library<STD_LIB>` as well as the
two :ref:`dummy protocol suites<DUMMY_BOOL>`. It essentially contains all you
need to be able to run a secure computation.

The suite project contains all the actually secure protocol suites that FRESCO
currently can run.

Demos are demonstrators which shows different applications in actual use. It
should be fairly straightforward to take the jar file these generate and run
them on different machines. The AES demo is chosen as the :ref:`quickstart
tutorial<Quickstart>`, where we go into greater depths of the inner workings.

ScapiNetwork is an example of how to create a different network implementation
and use this instead.

Tools contains projects that either helps the developer or is a standalone tool
which can help in certain situations. 
