package com.vendora;
import jakarta.persistence.*; import java.time.Instant;
@Entity
public class AuditLog {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  public Long actorId; public String action, target; public Instant createdAt=Instant.now();
}
