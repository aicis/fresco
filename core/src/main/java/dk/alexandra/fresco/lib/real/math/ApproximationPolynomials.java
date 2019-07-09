package dk.alexandra.fresco.lib.real.math;

public class ApproximationPolynomials {

  /**
   * p2607 from "Computer Approximations" by Hart et al. which approximates the natural logarithm on
   * the interval [0.5, 1].
   */
  public static double[] LOG = new double[] {-0.30674666858e1, 0.1130516183486e2,
      -0.2774666470302e2, 0.5149518504454e2, -0.6669583732238e2, 0.5853503340958e2,
      -0.3320167436859e2, 0.1098927015084e2, -0.161300738935e1};

  /**
   * p0132 from "Computer Approximations" by Hart et al. which approximates the square root on the
   * interval [0.5, 1].
   */
  public static double[] SQRT =
      new double[] {0.22906994529, 1.300669049, -0.9093210498, 0.5010420763, -0.1214683824};

  /**
   * p1045 from "Computer Approximations" by Hart et al. which approximates x -> 2^x
   * on the interval [0, 1].
   */
  public static double[] TWOPOW =
      new double[] {0.1000000077443021686e1, 0.693147180426163827795756e0, 0.24022651071017064605384e0,
          .55504068620466379157744e-1, 0.9618341225880462374977e-2, 0.1332730359281437819329e-2,
          0.155107460590052573978e-3, 0.14197847399765606711e-4, 0.1863347724137967076e-5};

}
