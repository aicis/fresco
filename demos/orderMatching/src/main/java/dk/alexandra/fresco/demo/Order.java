package dk.alexandra.fresco.demo;

public class Order implements Comparable {
  public Order(int userId, int limitRate, boolean buy) {
    this.userId = userId;
    this.limitRate = limitRate;
    this.buy = buy;
  }
  public final int userId;
  public final int limitRate;
  public final boolean buy; // True if it is a buy order, false if it is a sell order

  @Override
  public int compareTo(Object o) {
    if (!(o instanceof Order)) {
      return 1;
    }
    return Integer.valueOf(limitRate).compareTo(Integer.valueOf(((Order) o).limitRate));
  }
}