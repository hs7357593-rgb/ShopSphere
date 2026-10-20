package com.vendora;
import jakarta.persistence.*; import java.time.Instant; import java.util.*;
@Entity @Table(name="shop_order")
public class ShopOrder {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  public String checkoutId, sellerName, customerName, customerEmail, status="PLACED";
  public int total, commission; public Long customerId, sellerId; public Instant createdAt=Instant.now();
  @ElementCollection(fetch=FetchType.EAGER) public List<OrderLine> lines=new ArrayList<>();
}
