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
  $ cd fresco
  $ mvn install -DskipITs

This will download some dependencies, compile FRESCO, and run a number
of tests. If everything works fine Maven installs FRESCO on your
system and a FRESCO JAR file can now be found in the ``./target``
folder.

.. _scapi: https://github.com/cryptobiu/scapi


Persistent Store
----------------

If you don't care about running protocol suites in the
:term:`preprocessing model`, such as :ref:`SPDZ <SPDZ>`, you can just
ignore this section.

Protocols in the preprocessing model run in two phases, the *offline*
phase and the *online* phase. Data from the offline phase needs to be
available in the online phase. As a default, offline data gets stored
in memory, but this puts a limit to the amount of preprocessed data
you can store and it has the drawback that data is lost when the host
is switched off.

FRESCO also supports persistent storage via MySQL. To make use of this
feature you must have MySQL installed. You can, e.g., use the
following MySQL commands to set up the database: ::

    CREATE USER 'fresco'@'localhost' IDENTIFIED BY 'yourpassowrd';
    CREATE DATABASE IF NOT EXISTS fresco;
    GRANT ALL PRIVILEGES ON fresco.* TO 'fresco'@'localhost';

Then modify the files in ``properties/db.properties`` to match the
database, username, and password that you choose.

If you have set up a MySQL database like this, you can run ``mvn
install`` without the ``-DskipITs`` flag which will also run a few
tests of the MySQL connection.
