package com.ss871104.oauth2security.controllers;

import com.ss871104.oauth2security.dtos.input.*;
import com.ss871104.oauth2security.dtos.output.ErrorResponse;
import com.ss871104.oauth2security.models.AuthUser;
import com.ss871104.oauth2security.models.Permission;
import com.ss871104.oauth2security.models.Role;
import com.ss871104.oauth2security.security.redis.TokenRedisService;
import com.ss871104.oauth2security.security.token.JwtUtil;
import com.ss871104.oauth2security.security.token.TokenData;
import com.ss871104.oauth2security.services.AuthUserService;
import com.ss871104.oauth2security.services.PermissionService;
import com.ss871104.oauth2security.services.RoleService;
import com.ss871104.oauth2security.util.ServiceErrorException;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/auth-service/api/admin")
public class AdminController {
    @Autowired
    private AuthUserService authUserService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private TokenRedisService tokenRedisService;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/findAllAuthUsers")
    public ResponseEntity findAllAuthUsers() {
        try {
            List<AuthUser> authUserList = authUserService.findAllAuthUser();
            return new ResponseEntity(authUserList, HttpStatus.OK);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/findAuthUser/{oAuth2Id}")
    public ResponseEntity findAuthUserByOAuth2Id(@PathVariable("oAuth2Id") String oAuth2Id) {
        try {
            AuthUser authUser = authUserService.findAuthUserByOAuth2Id(oAuth2Id);
            return new ResponseEntity(authUser, HttpStatus.OK);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @PostMapping("/addRoleToAuthUser")
    public ResponseEntity addRoleToAuthUserByRoleId(@RequestBody UpdateRoleFromAuthUserDto updateRoleFromAuthUserDto) {
        try {
            Long roleId = updateRoleFromAuthUserDto.getRoleId();
            String oAuth2Id = updateRoleFromAuthUserDto.getOAuth2Id();
            authUserService.addRoleToAuthUser(roleId, oAuth2Id);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @DeleteMapping("/deleteRoleFromAuthUser")
    public ResponseEntity deleteRoleFromAuthUserByRoleId(@RequestBody UpdateRoleFromAuthUserDto updateRoleFromAuthUserDto) {
        try {
            Long roleId = updateRoleFromAuthUserDto.getRoleId();
            String oAuth2Id = updateRoleFromAuthUserDto.getOAuth2Id();
            authUserService.deleteRoleFromAuthUser(roleId, oAuth2Id);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @PutMapping("/blockAuthUser/{oAuth2Id}")
    public ResponseEntity blockAuthUser(@PathVariable("oAuth2Id") String oAuth2Id) {
        try {
            authUserService.blockAuthUser(oAuth2Id);
            tokenRedisService.removeAllAccessesByOAuth2Id(oAuth2Id);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @PutMapping("/unblockAuthUser/{oAuth2Id}")
    public ResponseEntity unblockAuthUser(@PathVariable("oAuth2Id") String oAuth2Id) {
        try {
            authUserService.unblockAuthUser(oAuth2Id);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/findAllRoles")
    public ResponseEntity findAllRoles() {
        try {
            List<Role> roleList = roleService.findAllRoles();
            return new ResponseEntity(roleList, HttpStatus.OK);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @PostMapping("/addRole")
    public ResponseEntity addRole(@RequestBody UpdateRoleDto updateRoleDto) {
        try {
            String roleName = updateRoleDto.getRoleName();
            roleService.addRole(roleName);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @PutMapping("/updateRoleName")
    public ResponseEntity updateRoleName(@RequestBody UpdateRoleDto updateRoleDto) {
        try {
            Long roleId = updateRoleDto.getRoleId();
            String roleName = updateRoleDto.getRoleName();
            roleService.updateRoleName(roleId, roleName);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @DeleteMapping("/deleteRole/{roleId}")
    public ResponseEntity deleteRole(@PathVariable("roleId") Long roleId) {
        try {
            roleService.deleteRole(roleId);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @PutMapping("/addPermissionToRole")
    public ResponseEntity addPermissionToRole(@RequestBody UpdatePermissionOfRoleDto updatePermissionOfRoleDto) {
        try {
            Long roleId = updatePermissionOfRoleDto.getRoleId();
            Long permissionId = updatePermissionOfRoleDto.getPermissionId();
            roleService.addPermissionToRole(roleId, permissionId);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @DeleteMapping("/deletePermissionFromRole")
    public ResponseEntity deletePermissionFromRole(@RequestBody UpdatePermissionOfRoleDto updatePermissionOfRoleDto) {
        try {
            Long roleId = updatePermissionOfRoleDto.getRoleId();
            Long permissionId = updatePermissionOfRoleDto.getPermissionId();
            roleService.deletePermissionFromRole(roleId, permissionId);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/findAllPermissions")
    public ResponseEntity findAllPermissions() {
        try {
            List<Permission> permissionList = permissionService.findAllPermissions();
            return new ResponseEntity(permissionList, HttpStatus.OK);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/findPermissions/{roleId}")
    public ResponseEntity findPermissionsByRoleId(@PathVariable("roleId") Long roleId) {
        try {
            List<Permission> permissionList = permissionService.findPermissionsByRoleId(roleId);
            return new ResponseEntity(permissionList, HttpStatus.OK);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @PostMapping("/addPermission")
    public ResponseEntity addPermission(@RequestBody AddPermissionDto addPermissionDto) {
        try {
            permissionService.addPermission(addPermissionDto);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @PutMapping("/updatePermission")
    public ResponseEntity updatePermission(@RequestBody UpdatePermissionDto updatePermissionDto) {
        try {
            permissionService.updatePermission(updatePermissionDto);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @DeleteMapping("/deletePermission/{permissionId}")
    public ResponseEntity deletePermission(@PathVariable("permissionId") Long permissionId) {
        try {
            permissionService.deletePermission(permissionId);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (ServiceErrorException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
            return new ResponseEntity(errorResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/findAccesses/{oAuth2Id}")
    public ResponseEntity findAccessesByOAuth2Id(@PathVariable("oAuth2Id") String oAuth2Id) {
        try {
            List<TokenData> tokenDataList = new ArrayList<>();
            Set<String>  refreshTokenSet = tokenRedisService.getRefreshTokenSetByOAuth2Id(oAuth2Id);

            refreshTokenSet.forEach(r -> {
                Claims claims = jwtUtil.extractAllClaims(r);
                TokenData tokenData = new TokenData();
                String accessToken = tokenRedisService.getAccessTokenByRefreshToken(r);
                tokenData.setAccessToken(accessToken);
                tokenData.setOAuth2Id(claims.getSubject());
                tokenData.setRoles(claims.get("roles").toString());
                tokenData.setProvider(claims.get("provider").toString());
                tokenData.setDevice(claims.get("device").toString());
                tokenData.setHost(claims.get("host").toString());
                tokenDataList.add(tokenData);
            });
            return new ResponseEntity<>(tokenDataList, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.OK);
        }
    }
}
