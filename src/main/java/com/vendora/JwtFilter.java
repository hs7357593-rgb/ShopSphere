package com.vendora;
import io.jsonwebtoken.JwtException; import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.IOException; import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
@Component
public class JwtFilter extends OncePerRequestFilter {
  private final JwtService jwt; private final UserRepository users;
  public JwtFilter(JwtService j, UserRepository u){jwt=j;users=u;}
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
    String h=req.getHeader("Authorization");
    if(h!=null&&h.startsWith("Bearer ")){
      try{
        // role and enabled flag come from the database, not from the token, so suspensions apply immediately
        users.findById(jwt.userId(h.substring(7))).filter(u->u.enabled).ifPresent(u->
          SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            new AuthUser(u.id,u.email,u.role),null,List.of(new SimpleGrantedAuthority("ROLE_"+u.role.name())))));
      }catch(JwtException|IllegalArgumentException e){ /* invalid or expired token: request stays anonymous */ }
    }
    chain.doFilter(req,res);
  }
}
