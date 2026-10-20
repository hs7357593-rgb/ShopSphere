package com.vendora;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*; import java.util.concurrent.*; import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus; import org.springframework.test.context.ActiveProfiles; import org.springframework.web.server.ResponseStatusException;
@SpringBootTest @ActiveProfiles("test")
class OrderServiceTest {
  @Autowired OrderService service; @Autowired ProductRepository products; @Autowired UserRepository users;
  AuthUser customer(){ AppUser u=users.findByEmail("customer@vendora.local").orElseThrow(); return new AuthUser(u.id,u.email,u.role); }
  Product byName(String n){ return products.findAll().stream().filter(p->p.name.equals(n)).findFirst().orElseThrow(); }
  OrderService.Request req(Long id,int q){ return new OrderService.Request(List.of(new OrderService.Item(id,q))); }
  @Test void multiSellerCartIsSplitAndStockDecrements(){
    Product a=byName("Aero Headphones"), b=byName("Street Sneakers"); int sa=a.stock, sb=b.stock;
    List<ShopOrder> res=service.checkout(customer(),new OrderService.Request(List.of(new OrderService.Item(a.id,2),new OrderService.Item(b.id,1))));
    assertEquals(2,res.size());
    assertTrue(res.stream().allMatch(o->o.commission==o.total*10/100));
    assertEquals(sa-2,products.findById(a.id).orElseThrow().stock); assertEquals(sb-1,products.findById(b.id).orElseThrow().stock); }
  @Test void cannotOversell(){
    Product p=byName("Yoga Mat Pro"); int before=p.stock;
    ResponseStatusException ex=assertThrows(ResponseStatusException.class,()->service.checkout(customer(),req(p.id,before+1)));
    assertEquals(HttpStatus.CONFLICT,ex.getStatusCode()); assertEquals(before,products.findById(p.id).orElseThrow().stock); }
  @Test void concurrentOrdersForTheLastItemOnlyOneSucceeds() throws Exception {
    Product p=byName("Black Blazer Set"); p.stock=1; products.save(p);
    Callable<Boolean> c=()->{ try{ service.checkout(customer(),req(p.id,1)); return true; }catch(ResponseStatusException e){ return false; } };
    ExecutorService ex=Executors.newFixedThreadPool(2); long ok=0; for(Future<Boolean> f:ex.invokeAll(List.of(c,c))) if(f.get()) ok++; ex.shutdown();
    assertEquals(1,ok); assertEquals(0,products.findById(p.id).orElseThrow().stock); }
  @Test void cancelRestoresStockAndOtherUsersCannotCancel(){
    Product p=byName("Cricket Shoes"); int before=p.stock;
    ShopOrder o=service.checkout(customer(),req(p.id,3)).get(0); assertEquals(before-3,products.findById(p.id).orElseThrow().stock);
    AppUser admin=users.findByEmail("admin@vendora.local").orElseThrow();
    assertThrows(ResponseStatusException.class,()->service.cancel(new AuthUser(admin.id,admin.email,admin.role),o.id));
    service.cancel(customer(),o.id); assertEquals(before,products.findById(p.id).orElseThrow().stock); }
}
