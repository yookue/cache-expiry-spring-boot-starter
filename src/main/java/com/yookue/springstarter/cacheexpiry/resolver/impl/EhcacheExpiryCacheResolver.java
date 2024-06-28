/*
 * Copyright (c) 2020 Yookue Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yookue.springstarter.cacheexpiry.resolver.impl;


import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import com.yookue.springstarter.cacheexpiry.resolver.ExpiryCacheResolver;
import com.yookue.springstarter.cacheexpiry.util.CacheExpiryDetectionUtils;
import com.yookue.springstarter.cacheexpiry.util.CacheOperationContextUtils;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.ConfigurationHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;


/**
 * {@link org.springframework.cache.interceptor.CacheResolver} for EhCache
 *
 * @author David Hsing
 * @reference "https://www.baeldung.com/ehcache"
 * @see org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration
 * @see org.springframework.cache.ehcache.EhCacheManagerUtils
 * @see net.sf.ehcache.config.ConfigurationFactory
 * @see net.sf.ehcache.config.ConfigurationHelper
 */
@Setter
@SuppressWarnings({"unused", "JavadocDeclaration", "JavadocLinkAsPlainText", "JavadocReference"})
public class EhcacheExpiryCacheResolver extends SimpleCacheResolver implements BeanFactoryAware, ExpiryCacheResolver {
    @Getter
    private boolean detectNameResolver = false;

    protected BeanFactory beanFactory;

    public EhcacheExpiryCacheResolver(@Nonnull CacheManager manager) {
        super(manager);
    }

    public EhcacheExpiryCacheResolver(@Nonnull CacheManager manager, boolean detectNameResolver) {
        super(manager);
        this.detectNameResolver = detectNameResolver;
    }

    @Nonnull
    @Override
    @SneakyThrows
    public Collection<? extends Cache> resolveCaches(@Nonnull CacheOperationInvocationContext<?> context) {
        CacheResolver cacheResolver = CacheOperationContextUtils.getCacheResolver(beanFactory, context);
        if (cacheResolver != null && !ClassUtils.isAssignableValue(getClass(), cacheResolver)) {
            return cacheResolver.resolveCaches(context);
        }
        CacheManager facadeCacheManager = ObjectUtils.defaultIfNull(CacheOperationContextUtils.getCacheManager(beanFactory, context), super.getCacheManager());
        Assert.isInstanceOf(EhCacheCacheManager.class, facadeCacheManager, "Cache manager must be an instanceof " + EhCacheCacheManager.class.getCanonicalName());
        net.sf.ehcache.CacheManager originCacheManager = ((EhCacheCacheManager) facadeCacheManager).getCacheManager();
        Assert.notNull(originCacheManager, "Cache manager for '" + ClassUtils.getQualifiedMethodName(context.getMethod()) + "' must not be null");
        ConfigurationHelper originConfigHelper = new ConfigurationHelper(originCacheManager, originCacheManager.getConfiguration());
        // Prepare caches and configuration
        Collection<String> cacheNames = CacheExpiryDetectionUtils.detectCacheNames(context, beanFactory, detectNameResolver);
        if (CollectionUtils.isEmpty(cacheNames)) {
            return Collections.emptyList();
        }
        CacheConfiguration cloneConfiguration = null;
        Duration duration = CacheExpiryDetectionUtils.detectCachePeriod(context);
        if (duration != null) {
            if (originCacheManager.getConfiguration() != null && originCacheManager.getConfiguration().getDefaultCacheConfiguration() != null) {
                cloneConfiguration = originCacheManager.getConfiguration().getDefaultCacheConfiguration().clone();
            } else {
                cloneConfiguration = new CacheConfiguration();
            }
            cloneConfiguration.setTimeToLiveSeconds(duration.getSeconds());
        }
        Collection<Cache> result = new ArrayList<>(cacheNames.size());
        for (String cacheName : cacheNames) {
            if (StringUtils.isBlank(cacheName)) {
                continue;
            }
            Cache facadeCache = null;
            if (duration != null) {
                cloneConfiguration.setName(cacheName);
                Ehcache originCache = (Ehcache) MethodUtils.invokeMethod(originConfigHelper, true, "createCache", cloneConfiguration);    // $NON-NLS-1$
                if (originCache != null) {
                    facadeCache = new EhCacheCache(originCache);
                }
            }
            if (facadeCache == null) {
                facadeCache = facadeCacheManager.getCache(cacheName);
            }
            if (facadeCache == null) {
                throw new IllegalStateException("Cannot find/create cache '" + cacheName + "' with operation " + context.getOperation());
            }
            result.add(facadeCache);
        }
        return result;
    }
}
