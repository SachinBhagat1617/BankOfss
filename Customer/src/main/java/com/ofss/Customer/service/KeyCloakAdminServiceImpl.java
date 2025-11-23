package com.ofss.Customer.service;

import com.ofss.Customer.dto.CustomerRequestDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeyCloakAdminServiceImpl implements KeyCloakAdminService{

    @Value("${keycloak.admin.username}")
    private String adminUsername;
    @Value("${keycloak.admin.password}")
    private String adminPassword;
    @Value("${keycloak.admin.client-id}")
    private String adminClientId;
    @Value("${keycloak.admin.realm}")
    private String realm;
    @Value("${keycloak.admin.server-url}")
    private String keyCloakServerUrl;
    @Value("${keycloak.admin.client-uid}")
    private String clientUid;

    private final RestTemplate restTemplate=new RestTemplate();

    @Override
    public String getAdminAccessToken() {

        MultiValueMap<String,String> params=new LinkedMultiValueMap<>();
        params.add("username",adminUsername);
        params.add("password",adminPassword);
        params.add("client_id",adminClientId);
        params.add("grant_type","password");
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String,String>> entity=new HttpEntity<>(params,headers);
        ResponseEntity<Map> response=restTemplate.postForEntity(
                keyCloakServerUrl+"/realms/"+realm+"/protocol/openid-connect/token",
                entity,
                Map.class
        );
        return (String) response.getBody().get("access_token");
    }

    @Override
    public String createUser(String token, CustomerRequestDTO customerRequestDTO) {
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        Map<String,Object>userPayload=new HashMap<>();
        userPayload.put("username",customerRequestDTO.getFirstName());
        userPayload.put("email",customerRequestDTO.getEmail());
        userPayload.put("firstName",customerRequestDTO.getFirstName());
        userPayload.put("lastName",customerRequestDTO.getLastName());
        userPayload.put("enabled",true);
        Map<String,Object>credentials=new HashMap<>();
        credentials.put("type","password");
        credentials.put("value",customerRequestDTO.getPassword());
        credentials.put("temporary",false);
        userPayload.put("credentials", List.of(credentials));

        // Map<String,Object> --> payload data_type
        HttpEntity<Map<String,Object>> entity=new HttpEntity<>(userPayload,headers); // its type is decided by what you send in body, headers is not counted but send along with body
        ResponseEntity<String> response=restTemplate.postForEntity(
                keyCloakServerUrl+"/admin/realms/"+realm+"/users",
                entity,
                String.class
        );
        if(!HttpStatus.CREATED.equals(response.getStatusCode())){
            throw new RuntimeException("Failed to create user in Keycloak");
        }
        //Extract keycloak user id from location header
        URI location =response.getHeaders().getLocation();
        if(location ==null){
            throw new RuntimeException("Failed to retrieve created user location from Keycloak response");
        }
        String path= location.getPath();
        return path.substring(path.lastIndexOf("/")+1);
    }

    @Override
    public Map<String,Object> getClientRolesRepresentation(String token, String roleName) {
        HttpHeaders headers=new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity=new HttpEntity<>(headers);
        ResponseEntity<Map>response=restTemplate.exchange(
                keyCloakServerUrl+"/admin/realms/"+realm+"/clients/"+clientUid+"/roles/"+roleName,
                HttpMethod.GET,
                entity,
                Map.class
        );
        //System.out.println(response.getBody()); // dataType Map<String,Object>
        if(!HttpStatus.OK.equals(response.getStatusCode())){
            throw new RuntimeException("Failed to fetch role representation from Keycloak");
        }
        return response.getBody();
    }
    @Override
    public Void assignClientRoleToUser(String username, String roleName, String userId){
        String token=getAdminAccessToken();
        Map<String,Object> roleRepresentation=getClientRolesRepresentation(token,roleName);
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<List<Map<String,Object>>> entity=new HttpEntity<>(List.of(roleRepresentation),headers);
        ResponseEntity<Void> response=restTemplate.postForEntity(
                keyCloakServerUrl+"/admin/realms/"+realm+"/users/"+userId+"/role-mappings/clients/"+clientUid,
                entity,
                Void.class
        );
        if(!response.getStatusCode().is2xxSuccessful()){
            throw new RuntimeException("Failed to assign role to user in Keycloak");
        }
        return null;
    }


    @Override
    public void login(String email, String password, HttpServletResponse response) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", email);
        params.add("password", password);
        params.add("client_id", "OAuth2-PKCE");
        params.add("grant_type", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                keyCloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token",
                entity,
                Map.class
        );

        if (!HttpStatus.OK.equals(tokenResponse.getStatusCode())) {
            throw new RuntimeException("Login failed");
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        Cookie cookie = new Cookie("access_token", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true in production
        cookie.setPath("/");
        cookie.setMaxAge(60 * 15);
        response.addCookie(cookie);
    }

    @Override
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("access_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true in production
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
