package com.vendora;
import java.util.List;
import org.springframework.data.domain.Sort; import org.springframework.http.HttpStatus; import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/admin")
public class AdminController {
  record SellerView(Long id, String storeName, String status, String email){}
  private final SellerRepository sellers; private final UserRepository users; private final OrderRepository orders; private final AuditRepository audit;
  public AdminController(SellerRepository s, UserRepository u, OrderRepository o, AuditRepository a){sellers=s;users=u;orders=o;audit=a;}
  private SellerView view(SellerProfile s){ return new SellerView(s.id,s.storeName,s.status.name(),users.findById(s.userId).map(u->u.email).orElse("")); }
  @GetMapping("/sellers") public List<SellerView> sellers(){ return sellers.findAll().stream().map(this::view).toList(); }
  @PostMapping("/sellers/{id}/{action}") @Transactional
  public SellerView decide(@AuthenticationPrincipal AuthUser a, @PathVariable Long id, @PathVariable String action){
    SellerStatus st=switch(action){ case "approve"->SellerStatus.APPROVED; case "reject"->SellerStatus.REJECTED; case "suspend"->SellerStatus.SUSPENDED;
      default->throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown action"); };
    SellerProfile s=sellers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Seller not found"));
    s.status=st; sellers.save(s);
    AuditLog l=new AuditLog(); l.actorId=a.id(); l.action="SELLER_"+st.name(); l.target="seller:"+id; audit.save(l);
    return view(s); }
  @GetMapping("/orders") public List<ShopOrder> orders(){ return orders.findAll(Sort.by(Sort.Direction.DESC,"id")); }
  @GetMapping("/audit-logs") public List<AuditLog> auditLogs(){ return audit.findAll(Sort.by(Sort.Direction.DESC,"id")); }
}
