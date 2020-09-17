package dk.alexandra.fresco.demo;

public class OrderMatch implements Comparable {
  public final int firstId;
  public final int secondId;
  public final int rate;

  public OrderMatch(int firstId, int secondId, int rate) {
    this.firstId = firstId;
    this.secondId = secondId;
    this.rate = rate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrderMatch that = (OrderMatch) o;
    return that.rate == this.rate && (
        that.firstId == this.firstId || that.firstId == this.secondId) && (
        that.secondId == this.firstId || that.secondId == this.secondId);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + rate;
    hash = 31 * hash + firstId + secondId;
    return hash;
  }

  @Override
  public int compareTo(Object o) {
    if (!(o instanceof OrderMatch)) {
      return 1;
    }
    int res = Integer.valueOf(rate).compareTo(Integer.valueOf(((OrderMatch) o).rate));
    if (res == 0) {
      res = Integer.valueOf(Math.min(firstId, secondId)).compareTo(
          Integer.valueOf(Math.min(((OrderMatch) o).firstId, ((OrderMatch) o).secondId)));
    }
    return res;
  }
}
