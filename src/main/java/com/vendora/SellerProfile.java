package com.vendora;
import jakarta.persistence.*; import java.time.Instant;
@Entity
public class SellerProfile {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(unique=true, nullable=false) public Long userId;
  @Column(unique=true, nullable=false) public String storeName;
  @Enumerated(EnumType.STRING) @Column(nullable=false) public SellerStatus status=SellerStatus.PENDING;
  public Instant createdAt=Instant.now();
}
