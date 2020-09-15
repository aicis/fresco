package dk.alexandra.fresco.demo;

public class OrderMatch {
  public OrderMatch(int firstId, int secondId, int rate) {
    this.firstId = firstId;
    this.secondId = secondId;
    this.rate = rate;
  }
  public final int firstId;
  public final int secondId;
  public final int rate;

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof OrderMatch)) {
      return false;
    }
    return ((OrderMatch) other).rate == this.rate && (
        ((OrderMatch) other).firstId == this.firstId || ((OrderMatch) other).firstId == this.secondId) && (
        ((OrderMatch) other).secondId == this.firstId || ((OrderMatch) other).secondId == this.secondId);
  }
}
