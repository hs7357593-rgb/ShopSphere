package com.vendora;
import java.util.List; import java.util.UUID; import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.CommandLineRunner; import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Component;
@Component @Order(1)
public class UserSeeder implements CommandLineRunner {
  private static final Logger log=LoggerFactory.getLogger(UserSeeder.class);
  private final UserRepository users; private final SellerRepository sellers; private final PasswordEncoder enc; private final String pw;
  public UserSeeder(UserRepository u, SellerRepository s, PasswordEncoder e, @Value("${app.seed.password:}") String pw){users=u;sellers=s;enc=e;this.pw=pw;}
  private AppUser make(String email,String name,Role role,String password,boolean live){
    AppUser u=new AppUser(); u.email=email; u.name=name; u.role=role; u.passwordHash=enc.encode(password); u.enabled=live; return users.save(u); }
  public void run(String... a){
    if(users.count()>0) return;
    boolean live=!pw.isBlank(); String p=live?pw:UUID.randomUUID().toString();
    make("admin@vendora.local","Admin",Role.ADMIN,p,live);
    make("customer@vendora.local","Demo Customer",Role.CUSTOMER,p,live);
    for(String store:List.of("SoundForge","UrbanSole","Chronos","Lumen Home","KeyCraft","FlexFit","ViewMax","HomeTech")){
      AppUser u=make(store.toLowerCase().replace(" ","")+"@vendora.local",store+" Owner",Role.SELLER,p,live);
      SellerProfile sp=new SellerProfile(); sp.userId=u.id; sp.storeName=store; sp.status=SellerStatus.APPROVED; sellers.save(sp); }
    if(!live) log.warn("SEED_PASSWORD is not set: seed accounts were created disabled. Set it and recreate the database to sign in as admin@vendora.local.");
  }
}
