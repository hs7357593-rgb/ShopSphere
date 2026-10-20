package com.vendora;
import java.util.*; import org.slf4j.*; import org.springframework.dao.DataIntegrityViolationException; import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException; import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; import org.springframework.web.servlet.resource.NoResourceFoundException;
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
  public record Err(int status, String message, Map<String,String> details){}
  private static ResponseEntity<Err> err(HttpStatusCode s,String m,Map<String,String> d){ return ResponseEntity.status(s).body(new Err(s.value(),m,d)); }
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Err> invalid(MethodArgumentNotValidException e){
    Map<String,String> d=new LinkedHashMap<>(); e.getBindingResult().getFieldErrors().forEach(f->d.putIfAbsent(f.getField(),f.getDefaultMessage()));
    return err(HttpStatus.BAD_REQUEST,"Validation failed",d); }
  @ExceptionHandler(ResponseStatusException.class) ResponseEntity<Err> status(ResponseStatusException e){ return err(e.getStatusCode(),e.getReason(),null); }
  @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<Err> unreadable(HttpMessageNotReadableException e){ return err(HttpStatus.BAD_REQUEST,"Malformed request body",null); }
  @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<Err> conflict(DataIntegrityViolationException e){ return err(HttpStatus.CONFLICT,"Conflicts with existing data",null); }
  @ExceptionHandler(AccessDeniedException.class) ResponseEntity<Err> denied(AccessDeniedException e){ return err(HttpStatus.FORBIDDEN,"You do not have access to this resource",null); }
  @ExceptionHandler(NoResourceFoundException.class) ResponseEntity<Err> missing(NoResourceFoundException e){ return err(HttpStatus.NOT_FOUND,"Not found",null); }
  @ExceptionHandler(Exception.class) ResponseEntity<Err> other(Exception e){ log.error("Unhandled error",e); return err(HttpStatus.INTERNAL_SERVER_ERROR,"Unexpected server error",null); }
}
