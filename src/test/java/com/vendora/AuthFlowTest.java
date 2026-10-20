package com.vendora;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.*; import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest; import org.springframework.http.MediaType; import org.springframework.test.context.ActiveProfiles; import org.springframework.test.web.servlet.MockMvc;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class AuthFlowTest {
  static final String PW="Test-Passw0rd!";
  @Autowired MockMvc mvc; @Autowired ObjectMapper om;
  String send(String url,String body,int expected,String token) throws Exception {
    var rb=post(url).contentType(MediaType.APPLICATION_JSON).content(body); if(token!=null) rb.header("Authorization","Bearer "+token);
    return mvc.perform(rb).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString(); }
  String login(String email) throws Exception {
    return om.readTree(send("/api/auth/login","{\"email\":\""+email+"\",\"password\":\""+PW+"\"}",200,null)).get("token").asText(); }
  @Test void registerLoginAndDuplicateEmail() throws Exception {
    String b="{\"email\":\"a@x.com\",\"password\":\"longpassword1\",\"name\":\"A\"}";
    send("/api/auth/register",b,201,null); send("/api/auth/register",b,409,null);
    send("/api/auth/login","{\"email\":\"a@x.com\",\"password\":\"longpassword1\"}",200,null);
    send("/api/auth/login","{\"email\":\"a@x.com\",\"password\":\"wrong-password\"}",401,null); }
  @Test void shortPasswordIsRejected() throws Exception { send("/api/auth/register","{\"email\":\"b@x.com\",\"password\":\"short\",\"name\":\"B\"}",400,null); }
  @Test void ordersRequireLogin() throws Exception { mvc.perform(get("/api/orders")).andExpect(status().isUnauthorized()); }
  @Test void customerCannotUseAdminOrSellerApis() throws Exception {
    String t=login("customer@vendora.local");
    mvc.perform(get("/api/admin/sellers").header("Authorization","Bearer "+t)).andExpect(status().isForbidden());
    mvc.perform(get("/api/seller/products").header("Authorization","Bearer "+t)).andExpect(status().isForbidden()); }
  @Test void sellerCannotEditAnotherSellersProduct() throws Exception {
    String t=login("soundforge@vendora.local"); long other=0;
    for(JsonNode p:om.readTree(mvc.perform(get("/api/products?size=100")).andReturn().getResponse().getContentAsString()))
      if("UrbanSole".equals(p.get("sellerName").asText())) other=p.get("id").asLong();
    mvc.perform(put("/api/seller/products/"+other).header("Authorization","Bearer "+t).contentType(MediaType.APPLICATION_JSON)
      .content("{\"name\":\"Hacked\",\"category\":\"Fashion\",\"price\":1,\"oldPrice\":2,\"stock\":1}")).andExpect(status().isNotFound()); }
  @Test void newSellerNeedsAdminApproval() throws Exception {
    String t=om.readTree(send("/api/auth/register-seller","{\"email\":\"s@x.com\",\"password\":\"longpassword1\",\"name\":\"S\",\"storeName\":\"NewShop\"}",201,null)).get("token").asText();
    mvc.perform(get("/api/seller/products").header("Authorization","Bearer "+t)).andExpect(status().isForbidden());
    String admin=login("admin@vendora.local"); long id=0;
    for(JsonNode s:om.readTree(mvc.perform(get("/api/admin/sellers").header("Authorization","Bearer "+admin)).andReturn().getResponse().getContentAsString()))
      if("NewShop".equals(s.get("storeName").asText())) id=s.get("id").asLong();
    send("/api/admin/sellers/"+id+"/approve","",200,admin);
    mvc.perform(get("/api/seller/products").header("Authorization","Bearer "+t)).andExpect(status().isOk()); }
}
