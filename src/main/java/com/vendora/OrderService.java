package com.vendora;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.*;
import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.server.ResponseStatusException;
@Service
public class OrderService {
  public record Item(@NotNull Long productId, @Min(1) @Max(100) int quantity){}
  public record Request(@NotEmpty @Valid List<Item> items){}
  static final int COMMISSION_PERCENT=10;
  private final ProductRepository products; private final OrderRepository orders; private final UserRepository users;
  public OrderService(ProductRepository p, OrderRepository o, UserRepository u){products=p;orders=o;users=u;}

  /** One checkout becomes one order per seller. Customer identity, prices and commission all come from the server. */
  @Transactional
  public List<ShopOrder> checkout(AuthUser who, Request req){
    AppUser u=users.findById(who.id()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Unknown user"));
    Map<Long,Integer> qty=new TreeMap<>();
    for(Item i:req.items()) qty.merge(i.productId(),i.quantity(),Integer::sum);
    List<Product> locked=products.lockAll(qty.keySet());   // row locks, taken in id order, prevent overselling and deadlocks
    if(locked.size()!=qty.size()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Unknown product");
    String checkoutId=UUID.randomUUID().toString(); Map<Long,ShopOrder> bySeller=new LinkedHashMap<>();
    for(Product p:locked){
      if(!p.active||p.sellerId==null) throw new ResponseStatusException(HttpStatus.CONFLICT,p.name+" is not available");
      int q=qty.get(p.id);
      if(p.stock<q) throw new ResponseStatusException(HttpStatus.CONFLICT,"Not enough stock for "+p.name);
      p.stock-=q;
      ShopOrder o=bySeller.computeIfAbsent(p.sellerId,id->{ShopOrder n=new ShopOrder(); n.checkoutId=checkoutId; n.sellerId=id; n.sellerName=p.sellerName;
        n.customerId=u.id; n.customerName=u.name; n.customerEmail=u.email; return n;});
      OrderLine l=new OrderLine(); l.productId=p.id; l.name=p.name; l.quantity=q; l.price=p.price; o.lines.add(l); o.total+=p.price*q;
    }
    bySeller.values().forEach(o->o.commission=o.total*COMMISSION_PERCENT/100);
    return orders.saveAll(bySeller.values());
  }

  @Transactional
  public ShopOrder cancel(AuthUser who, Long id){
    ShopOrder o=orders.lockById(id).filter(x->who.id().equals(x.customerId)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));
    if(!"PLACED".equals(o.status)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Only orders that have not been packed can be cancelled");
    Map<Long,Integer> back=new TreeMap<>(); o.lines.forEach(l->back.merge(l.productId,l.quantity,Integer::sum));
    products.lockAll(back.keySet()).forEach(p->p.stock+=back.get(p.id));
    o.status="CANCELLED"; return orders.save(o);
  }
}
