package dk.alexandra.fresco.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlainOrderMatching {
  List<Order> orders;

  public PlainOrderMatching(List<Order> orders) {
    this.orders = orders;
  }

  public List<OrderMatch> compute() {
    // First sort the buy orders
    List<Order> buyOrders = orders.stream().filter(c -> c.buy == true).sorted()
        .collect(Collectors.toList());
    // Reverse to get highest orders first
    Collections.reverse(buyOrders);
    List<Order> sellOrders = orders.stream().filter(c -> c.buy == false).sorted()
        .collect(Collectors.toList());
    List<OrderMatch> res = new ArrayList<>();
    for (int i = 0; i < buyOrders.size(); i++) {
      if (buyOrders.get(i).limitRate >= sellOrders.get(i).limitRate) {
        int price = buyOrders.get(i).limitRate + sellOrders.get(i).limitRate >> 1;
        res.add(new OrderMatch(buyOrders.get(i).userId, sellOrders.get(i).userId, price));
      } else {
        break;
      }
    }
    return res;
  }
}
