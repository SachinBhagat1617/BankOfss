package com.ofss.Customer.service;

import com.ofss.Customer.dto.CustomerRequestDTO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface KeyCloakAdminService {
    String getAdminAccessToken();
    String  createUser(String token, CustomerRequestDTO customerRequestDTO);
    Map<String,Object> getClientRolesRepresentation(String token, String roleName);
    Void assignClientRoleToUser(String username,String roleName, String userId);
    void login(String email, String password, HttpServletResponse response);
    void logout(HttpServletResponse response);

}
