/**
 *
 * Contains common math functionality used in Overdrive.
 *
 * <p>
 * Specifically, functionality to represent and work with polynomials as required in the Overdrive
 * variant of the BGV encryption scheme. That is, polynomials in polynomial in the polynomial ring
 * <i>R<sub>q</sub> = Z<sub>q</sub>[X]/&Phi;<sub>m</sub>(X)</i> where <i>m</i> is some two-power and
 * <i>&Phi;<sub>m</sub>(X)</i> is the m'th cyclotomic polynomial and <i>q</i> is some prime modulus
 * <i>q &in; Z<sub>q</sub></i> so that <i>Z<sub>q</sub></i> contains an <i>m</i>'th root of unity.
 * </p>
 *
 * <p>
 * Also contains functionality to pick correct parameters for the protocol.
 * </p>
 */
package dk.alexandra.fresco.overdrive.math;
