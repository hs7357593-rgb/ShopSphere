package com.vendora;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.Map;
import org.springframework.http.HttpStatus; import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/auth")
public class AuthController {
  record RegisterRequest(@NotBlank @Email String email, @NotBlank @Size(min=8,max=72) String password, @NotBlank @Size(max=100) String name){}
  record SellerRegisterRequest(@NotBlank @Email String email, @NotBlank @Size(min=8,max=72) String password, @NotBlank @Size(max=100) String name, @NotBlank @Size(max=100) String storeName){}
  record LoginRequest(@NotBlank String email, @NotBlank String password){}
  record AuthResponse(String token, Long id, String name, String email, String role){}
  private final UserRepository users; private final SellerRepository sellers; private final PasswordEncoder enc; private final JwtService jwt;
  public AuthController(UserRepository u, SellerRepository s, PasswordEncoder e, JwtService j){users=u;sellers=s;enc=e;jwt=j;}
  private AppUser create(String email,String pw,String name,Role role){
    String e=email.trim().toLowerCase();
    if(users.existsByEmail(e)) throw new ResponseStatusException(HttpStatus.CONFLICT,"An account with this email already exists");
    AppUser u=new AppUser(); u.email=e; u.name=name.trim(); u.passwordHash=enc.encode(pw); u.role=role; return users.save(u); }
  private AuthResponse resp(AppUser u){ return new AuthResponse(jwt.issue(u),u.id,u.name,u.email,u.role.name()); }
  @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody RegisterRequest r){ return resp(create(r.email(),r.password(),r.name(),Role.CUSTOMER)); }
  @PostMapping("/register-seller") @ResponseStatus(HttpStatus.CREATED) @Transactional
  public AuthResponse registerSeller(@Valid @RequestBody SellerRegisterRequest r){
    if(sellers.existsByStoreName(r.storeName().trim())) throw new ResponseStatusException(HttpStatus.CONFLICT,"This store name is taken");
    AppUser u=create(r.email(),r.password(),r.name(),Role.SELLER);
    SellerProfile sp=new SellerProfile(); sp.userId=u.id; sp.storeName=r.storeName().trim(); sp.status=SellerStatus.PENDING; sellers.save(sp);
    return resp(u); }
  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest r){
    AppUser u=users.findByEmail(r.email().trim().toLowerCase()).orElse(null);
    if(u==null||!u.enabled||!enc.matches(r.password(),u.passwordHash)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");
    return resp(u); }
  @GetMapping("/me")
  public Map<String,Object> me(@AuthenticationPrincipal AuthUser a){ return Map.of("id",a.id(),"email",a.email(),"role",a.role().name()); }
}
