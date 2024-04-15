package com.ss871104.oauth2security.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss871104.oauth2security.models.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GlobalFunctions {
    public String convertAnythingToString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String convertRolesToroleNamesString(Set<Role> roles) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(roles.stream().map(Role::getName).collect(Collectors.toSet()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<String> convertStringToStringSet(String string) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(string, new TypeReference<Set<String>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<SimpleGrantedAuthority> convertStringToAuthorities(String rolesString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Set<String> roles = objectMapper.readValue(rolesString, new TypeReference<Set<String>>() {});
            return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
