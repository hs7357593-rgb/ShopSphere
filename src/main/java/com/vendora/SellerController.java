package com.vendora;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.*;
import org.springframework.http.HttpStatus; import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/seller")
public class SellerController {
  record ProductRequest(@NotBlank @Size(max=120) String name, @NotBlank @Size(max=60) String category, @Min(1) int price, @Min(1) int oldPrice, @Min(0) int stock, @Size(max=40) String gradient){}
  record Summary(long orders, long revenue, long commission, long payout, List<Product> lowStock){}
  private static final Map<String,String> NEXT=Map.of("PLACED","PACKED","PACKED","SHIPPED","SHIPPED","DELIVERED");
  private final SellerRepository sellers; private final ProductRepository products; private final OrderRepository orders;
  public SellerController(SellerRepository s, ProductRepository p, OrderRepository o){sellers=s;products=p;orders=o;}
  private ResponseStatusException notFound(){ return new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found"); }
  private SellerProfile me(AuthUser a){
    SellerProfile s=sellers.findByUserId(a.id()).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,"No seller profile"));
    if(s.status!=SellerStatus.APPROVED) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Seller account is "+s.status.name().toLowerCase());
    return s; }
  private void apply(Product p, ProductRequest r){
    if(r.oldPrice()<r.price()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Original price cannot be lower than the price");
    p.name=r.name().trim(); p.category=r.category().trim(); p.price=r.price(); p.oldPrice=r.oldPrice(); p.stock=r.stock();
    p.gradient=(r.gradient()==null||r.gradient().isBlank())?"#6d28d9,#22e5ff":r.gradient(); }
  @GetMapping("/products") public List<Product> mine(@AuthenticationPrincipal AuthUser a){ return products.findBySellerIdAndActiveTrue(me(a).id); }
  @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED)
  public Product create(@AuthenticationPrincipal AuthUser a, @Valid @RequestBody ProductRequest r){
    SellerProfile s=me(a); Product p=new Product(); apply(p,r); p.sellerId=s.id; p.sellerName=s.storeName; p.emoji=""; return products.save(p); }
  @PutMapping("/products/{id}")
  public Product update(@AuthenticationPrincipal AuthUser a, @PathVariable Long id, @Valid @RequestBody ProductRequest r){
    Product p=products.findByIdAndSellerIdAndActiveTrue(id,me(a).id).orElseThrow(this::notFound); apply(p,r); return products.save(p); }
  @DeleteMapping("/products/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@AuthenticationPrincipal AuthUser a, @PathVariable Long id){
    Product p=products.findByIdAndSellerIdAndActiveTrue(id,me(a).id).orElseThrow(this::notFound); p.active=false; products.save(p); }
  @GetMapping("/orders") public List<ShopOrder> orders(@AuthenticationPrincipal AuthUser a){ return orders.findBySellerIdOrderByIdDesc(me(a).id); }
  @PatchMapping("/orders/{id}/status") @Transactional
  public ShopOrder advance(@AuthenticationPrincipal AuthUser a, @PathVariable Long id, @RequestParam String status){
    SellerProfile s=me(a); ShopOrder o=orders.lockById(id).filter(x->s.id.equals(x.sellerId)).orElseThrow(this::notFound);
    if(!status.equals(NEXT.get(o.status))) throw new ResponseStatusException(HttpStatus.CONFLICT,"Cannot move an order from "+o.status+" to "+status);
    o.status=status; return o; }
  @GetMapping("/summary")
  public Summary summary(@AuthenticationPrincipal AuthUser a){
    SellerProfile s=me(a); List<ShopOrder> os=orders.findBySellerIdOrderByIdDesc(s.id).stream().filter(o->!"CANCELLED".equals(o.status)).toList();
    long rev=os.stream().mapToLong(o->o.total).sum(), com=os.stream().mapToLong(o->o.commission).sum();
    return new Summary(os.size(),rev,com,rev-com,products.findBySellerIdAndActiveTrue(s.id).stream().filter(p->p.stock<=5).toList()); }
}
