package com.vendora;
import jakarta.servlet.http.HttpServletResponse; import java.util.*;
import org.springframework.data.domain.*; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api")
public class ProductController {
  private static final Set<String> SORTS=Set.of("id","price","rating","name");
  private final ProductRepository products; public ProductController(ProductRepository p){products=p;}
  @GetMapping("/products")
  public List<Product> list(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="") String category,
      @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size, @RequestParam(defaultValue="id") String sort, HttpServletResponse res){
    if(!SORTS.contains(sort)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown sort field");
    Page<Product> p=products.search(q.trim(),category.trim(),PageRequest.of(Math.max(page,0),Math.min(Math.max(size,1),100),Sort.by(sort)));
    res.setHeader("X-Total-Count",String.valueOf(p.getTotalElements())); return p.getContent(); }
  @GetMapping("/products/{id}")
  public Product one(@PathVariable Long id){ return products.findById(id).filter(p->p.active).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found")); }
  @GetMapping("/categories") public List<String> categories(){ return products.categories(); }
}
