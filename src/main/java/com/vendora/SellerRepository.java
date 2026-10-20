package com.vendora;
import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface SellerRepository extends JpaRepository<SellerProfile,Long> {
  Optional<SellerProfile> findByUserId(Long userId); Optional<SellerProfile> findByStoreName(String storeName); boolean existsByStoreName(String storeName); }
