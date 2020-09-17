package dk.alexandra.fresco.demo;

import java.util.Objects;

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
    int res = Integer.valueOf(limitRate).compareTo(Integer.valueOf(((Order) o).limitRate));
    if (res == 0) {
      res = Integer.valueOf(userId).compareTo(Integer.valueOf(((Order) o).userId));
    }
    if (res == 0) {
      res = Boolean.valueOf(buy).compareTo(Boolean.valueOf(((Order) o).buy));
    }
    return res;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Order order = (Order) o;
    return userId == order.userId &&
        limitRate == order.limitRate &&
        buy == order.buy;
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, limitRate, buy);
  }
}