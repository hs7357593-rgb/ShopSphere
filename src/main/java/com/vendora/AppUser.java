package com.vendora;
import jakarta.persistence.*;
@Entity @Table(name="app_user", uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class AppUser {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(nullable=false) public String email, name, passwordHash;
  @Enumerated(EnumType.STRING) @Column(nullable=false) public Role role;
  public boolean enabled=true;
}
