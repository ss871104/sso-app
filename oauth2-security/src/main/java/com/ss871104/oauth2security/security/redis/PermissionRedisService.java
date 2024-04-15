package com.ss871104.oauth2security.security.redis;

import com.ss871104.oauth2security.util.GlobalFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PermissionRedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GlobalFunctions globalFunctions;

    public void storeAllowPermissionUrlsByRoleNames(Set<String> roleNames, Set<String> permissionUrls) {
        String roleNamesString = globalFunctions.convertAnythingToString(roleNames);
        String permissionUrlsString = globalFunctions.convertAnythingToString(permissionUrls);
        redisTemplate.opsForValue().set("role_names_for_allowed_urls:" + roleNamesString, permissionUrlsString);
    }

    public Set<String> getAllowPermissionUrlsByRoleNames(Set<String> roleNames) {
        String key = globalFunctions.convertAnythingToString(roleNames);
        String permissionUrlsString = redisTemplate.opsForValue().get("role_names_for_allowed_urls:" + key);
        if (permissionUrlsString == null) {
            return null;
        }
        return globalFunctions.convertStringToStringSet(permissionUrlsString);
    }

    public void removeAllowPermissionUrlsByRoleNames() {
        Set<String> keys = redisTemplate.keys("role_names_for_allowed_urls:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
