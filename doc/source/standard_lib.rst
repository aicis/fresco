.. _STD_LIB:

Standard Library
================

FRESCO contains various functions which can be used freely as part of a secure
computation application. We have split the functions into two groups: binary and
arithmetic functions. This means that an application has to state which type of
application it is: binary or arithmetic.

The library contains at least these functionalities, but others may have been
added later, so be sure to check the ``ProtocolBuilderBinary`` and the
``ProtocolBuilderNumeric`` classes for additions if you cannot find what you
need here.

Binary functions
----------------

Apart from the basic functionalities that a binary protocol suite has to
implement (AND, XOR, NOT, random bit, input, output), FRESCO provides the
following binary functions:

**Advanced Functions**

- OR
- XNOR
- NAND
- Conditional Select
  
  + Choose either bit a or b from a given choice bit c.
- 1-bit half adder
- 1-bit full adder
- Full adder
  
  + Computes a+b where a and b are numbers represented by bits
- Multiplication
  
  + Computes a*b where a and b are numbers represented by bits 
- Log (base 2)
- BitIncrement
  
  + Increments a number represented by bits by 1
- Keyed compare and swap
  
  + Compares the keys of two key-value pairs and produce a list of pairs such
    that the first pair has the largest key.

**Comparison**

- Greater Than
  
  + Computes a > b where a and b are numbers represented by bits
- Equal
  
  + Computes a == b where a and b are numbers represented by bits

**Bristol**

Applications described by `Bristol
<https://www.cs.bris.ac.uk/Research/CryptographySecurity/MPC/>`_ and parsed
within FRESCO.

- 32x32 mutliplication
- AES
- SHA1
- SHA256
- DES
- MD5

**Debug**

- Open and print

  + Opens a secret shared boolean/list of booleans and prints them along with a
    chosen message once evaluation reaches this function
- Marker
  + Prints a message when evaluated

Arithmetic functions
--------------------

Apart from the basic functionality that any arithmetic protocol suite needs to
implement (\+, \-, \*, random bit, random element, input, output), FRESCO provides
the following functions for arithmetic applications:

**Advanced Functions**

- Sum
  
  + Computes the sum of a list of numbers
- Product
  
  + Computes the product of a list of numbers
- Division
- Modulus
- ToBits
  
  + Converts a number into it's bit representation
- Exponentiation
- Square root
- Natural Log
- Dot product
- Right shift
- Right shift with remainder
- Bit length
  
  + Computes the bit length of a secret shared number. Needs to know the maximum
    bit length of the number.
- Invert

**Comparison**

- Equal
- LEQ
- Compare to zero
- sign

**Debug**

- Open and print
  
  + Opens a secret shared number/list of numbers/matrix of numbers and prints
    them along with a chosen message once evaluation reaches this function
- Marker
  + Prints a message when evaluated
