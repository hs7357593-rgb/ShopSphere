package com.vendora;
import jakarta.validation.Valid; import java.util.List;
import org.springframework.http.HttpStatus; import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/orders")
public class OrderController {
  private final OrderService service; private final OrderRepository orders;
  public OrderController(OrderService s, OrderRepository o){service=s;orders=o;}
  @PostMapping @ResponseStatus(HttpStatus.CREATED)
  public List<ShopOrder> place(@AuthenticationPrincipal AuthUser a, @Valid @RequestBody OrderService.Request r){ return service.checkout(a,r); }
  @GetMapping public List<ShopOrder> mine(@AuthenticationPrincipal AuthUser a){ return orders.findByCustomerIdOrderByIdDesc(a.id()); }
  @PostMapping("/{id}/cancel") public ShopOrder cancel(@AuthenticationPrincipal AuthUser a, @PathVariable Long id){ return service.cancel(a,id); }
}
