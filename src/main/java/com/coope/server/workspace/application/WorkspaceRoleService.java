package com.coope.server.workspace.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkspaceRoleService {

    private final CacheManager cacheManager;

    public void clearUserRoleCache(Long workspaceId, Long userId) {
        Cache cache = cacheManager.getCache("workspaceRole");
        if (cache != null) {
            String key = workspaceId + ":" + userId;
            cache.evict(key);
            log.info("[Cache] 권한 캐시 제거 완료: {}", key);
        }
    }
}