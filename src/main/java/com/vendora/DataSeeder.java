package com.vendora;
import java.util.List; import org.springframework.boot.CommandLineRunner; import org.springframework.stereotype.Component;
@Component @org.springframework.core.annotation.Order(2)
public class DataSeeder implements CommandLineRunner {
  private final ProductRepository repo; private final SellerRepository sellers;
  public DataSeeder(ProductRepository r, SellerRepository s){repo=r;sellers=s;}
  public void run(String... a){
    if(repo.count()==18 && repo.findAll().stream().noneMatch(p->p.sellerId==null) && repo.findAll().stream().anyMatch(p->"One8 Shoes".equals(p.name))) return;   // already seeded with the current catalogue
    repo.deleteAll();
    List<Product> all=List.of(
    Product.of("Aero Headphones","Electronics","SoundForge",4999,7999,4.7,"","#6d28d9,#22e5ff",25),
    Product.of("Smart Watch X","Electronics","Chronos",8999,12999,4.6,"","#1d4ed8,#22e5ff",15),
    Product.of("Ceramic Lamp","Home","Lumen Home",1799,2499,4.4,"","#b45309,#f59e0b",30),
    Product.of("Foldable Duffel Bag","Fashion","UrbanSole",2999,3999,4.8,"","#0f766e,#22e5ff",20),
    Product.of("Mechanical Keyboard","Electronics","KeyCraft",5499,6999,4.9,"","#4338ca,#a78bfa",18),
    Product.of("Aroma Diffuser","Home","Lumen Home",1299,1799,4.3,"","#15803d,#86efac",50),
    Product.of("Yoga Mat Pro","Fitness","FlexFit",999,1499,4.5,"","#9333ea,#ec4899",60),
    Product.of("HD Smart TV","Electronics","ViewMax",13999,18999,4.6,"","#1d4ed8,#6d28d9",10),
    Product.of("Cricket Shoes","Fitness","FlexFit",2499,3499,4.4,"","#1d4ed8,#22e5ff",35),
    Product.of("Street Sneakers","Fashion","UrbanSole",3999,5499,4.6,"","#0d9488,#38bdf8",28),
    Product.of("TV Unit with Stand","Home","Lumen Home",7999,10999,4.5,"","#92400e,#d97706",12),
    Product.of("Peacock Wall Mural","Home","Lumen Home",5999,8499,4.7,"","#38bdf8,#86efac",22),
    Product.of("Front-Load Washing Machine","Electronics","HomeTech",28999,36999,4.5,"","#475569,#94a3b8",8),
    Product.of("Leather Formal Shoes","Fashion","UrbanSole",2799,3999,4.4,"","#92400e,#fbbf24",32),
    Product.of("Black Blazer Set","Fashion","UrbanSole",4999,7499,4.6,"","#1f2937,#6b7280",14),
    Product.of("Smart LED TV 43 inch","Electronics","ViewMax",17999,24999,4.4,"","#1f2937,#6366f1",9),
    Product.of("Oversized Black Tee","Fashion","UrbanSole",999,1499,4.5,"","#111827,#7c3aed",45),
    Product.of("One8 Shoes","Fashion","UrbanSole",9940,9940,4.5,"","#7f1d1d,#ef4444",25));
    all.forEach(p->p.sellerId=sellers.findByStoreName(p.sellerName).map(s->s.id).orElse(null));
    repo.saveAll(all);
  }
}
