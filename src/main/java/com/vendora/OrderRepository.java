package com.vendora;
import jakarta.persistence.LockModeType; import java.util.*;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
public interface OrderRepository extends JpaRepository<ShopOrder,Long> {
  List<ShopOrder> findByCustomerIdOrderByIdDesc(Long customerId);
  List<ShopOrder> findBySellerIdOrderByIdDesc(Long sellerId);
  @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select o from ShopOrder o where o.id=:id")
  Optional<ShopOrder> lockById(@Param("id") Long id);
}
