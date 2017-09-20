
Glossary
========

This section contains a description of some of the core concepts in FRESCO.

.. glossary::

   computation

      A specification of how a number of parties perform a given task
      by doing computation and possibly interacting.

   protocol suite

      A collection of protocols that is designed to be used together
      in order to do secure computation. An example is the :ref:`SPDZ
      <SPDZ>` protocol suite.

   native protocol
   composite protocol

      A *native* protocol is a protocol that is supported directly by a given
      protocol suite. The SPDZ suite contains native protocols for addition and
      multiplication, for example. A *composite* protocol is a protocol that is
      built from simpler protocols. For example, in the SPDZ suite you can
      easily obtain a composite protocol for comparison, or sorting.

   application

      An application is a protocol that performs some task. In that sense any
      protocol in FRESCO is an application. But there are many protocols and not
      all will make sense to those that want to use FRESCO for secure
      computation. we have included the ``Application`` interface which marks
      the class as a runnable secure computation by FRESCO. An example of an
      application is the ``AESDemo`` from the ``dk.alexandra.fresco.demo``
      package.

   computation parties

      This is the parties needed to carry out a protocol suite. For
      example, two or more computation parties are required to run
      the :ref:`SPDZ <SPDZ>` protocol suite.

   input/output parties

      This is parties that give input to and/or receive output from a secure
      computation. These do not have to be members of the parties doing the
      actual secure computation.

   preprocessing model

      Some protocol suites such as :ref:`SPDZ <SPDZ>` are divided in
      two phases. In the *offline* phase the computation parties carry
      out computation that is *independent* of the input from the
      input parties. Later, in the *online* phase, the computation
      parties can then use the offline material to do a more efficient
      computation.

   reactive protocol suite
   reactive application

      A reactive computation is a computation where intermediate values are
      opened up and further computation may depend on the opened values. Some
      protocol suites such as :ref:`SPDZ <SPDZ>` support reactive computation
      while others do not.

   resource pool

      The resource pool contains information such as the party ID and number of
      parties in the computation, but also includes classes such as a randomness
      generator and most importantly the network. The network should be managed
      by the application developer. Internally FRESCO will assume the network is
      connected once an application is set to be evaluated.  
      
   secure computation engine (SCE)

      The core component of FRESCO. This is the class responsible for
      coordinating what should happen when an application is set to be
      running. It manages the evaluator, sets up the protocol suite, and handles
      the execution of applications using a Thread pool. More than one SCE can
      exist at any point in time since no state is shared. The one thing the SCE
      does not handle is the resource pool which includes the network. The
      application developer is responsible for connecting and disconnecting the
      network himself.

   security parameter
   statistical security parameter
   computational security parameter

      Protocol suites are essentially cryptographic protocols. Such
      protocols are usually parameterized by a *security
      parameter*. This is a positive integer that can vary. The
      intuition is that the protocol gets *exponentially* harder to
      break when the security parameter grows. Without going in too
      much detail, the *computational* security parameter, usually
      denoted by :math:`k`, roughly states that the protocol is
      designed such that an adversary must use at least :math:`2^k`
      operations to break it. Typical values for a practical, secure
      setup are :math:`k \in \{128, \dots, 256\}`. The *statistical*
      security parameter, denoted by :math:`s`, is not concerned with
      the computational power of the adversary, but roughly states
      that the probability that the protocol breaks is bounded by
      :math:`2^{-s}`. Typical values of :math:`s` are 40 or 80. For
      more on this, consult cryptography text books such as [KL14]_ or
      [CDN15]_.

   information theoretic security
   computational security
   perfect security

      Information theoretic security refers to a kind of security that
      is not dependent on the computational power of the
      adversary. There may be some risk of breaking the protocol, but
      this risk do not grow if the adversary achieves more processing
      power. *Perfect* security is information theoretic security
      where the probability of breaking the protocol is zero.
