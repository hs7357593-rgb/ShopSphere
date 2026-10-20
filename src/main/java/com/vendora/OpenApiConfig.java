package com.vendora;
import io.swagger.v3.oas.models.*; import io.swagger.v3.oas.models.info.Info; import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;
@Configuration
public class OpenApiConfig {
  @Bean OpenAPI api(){ return new OpenAPI().info(new Info().title("VENDORA API").version("1.0"))
    .components(new Components().addSecuritySchemes("bearer",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
    .addSecurityItem(new SecurityRequirement().addList("bearer")); }
}
