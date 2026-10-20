package com.vendora;
import jakarta.persistence.LockModeType; import java.util.*;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
public interface ProductRepository extends JpaRepository<Product,Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id in :ids order by p.id")
  List<Product> lockAll(@Param("ids") Collection<Long> ids);
  @Query("select p from Product p where p.active=true and (:category='' or p.category=:category) and lower(p.name) like lower(concat('%',:q,'%'))")
  Page<Product> search(@Param("q") String q, @Param("category") String category, Pageable pageable);
  @Query("select distinct p.category from Product p where p.active=true order by p.category")
  List<String> categories();
  List<Product> findBySellerIdAndActiveTrue(Long sellerId);
  Optional<Product> findByIdAndSellerIdAndActiveTrue(Long id, Long sellerId);
}
