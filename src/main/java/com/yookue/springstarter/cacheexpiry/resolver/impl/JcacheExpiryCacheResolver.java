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
import java.util.concurrent.TimeUnit;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.jcache.JCacheCache;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import com.yookue.springstarter.cacheexpiry.resolver.ExpiryCacheResolver;
import com.yookue.springstarter.cacheexpiry.util.CacheExpiryDetectionUtils;
import com.yookue.springstarter.cacheexpiry.util.CacheOperationContextUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;


/**
 * {@link org.springframework.cache.interceptor.CacheResolver} for JCache
 *
 * @author David Hsing
 * @reference "https://www.baeldung.com/jcache"
 * @see org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration
 */
@Setter
@SuppressWarnings({"unused", "JavadocDeclaration", "JavadocLinkAsPlainText", "JavadocReference"})
public class JcacheExpiryCacheResolver extends SimpleCacheResolver implements BeanFactoryAware, ExpiryCacheResolver {
    @Getter
    private boolean detectNameResolver = false;

    @Getter
    private Configuration<?, ?> configuration;

    protected BeanFactory beanFactory;

    public JcacheExpiryCacheResolver(@Nonnull CacheManager manager) {
        super(manager);
    }

    public JcacheExpiryCacheResolver(@Nonnull CacheManager manager, boolean detectNameResolver) {
        super(manager);
        this.detectNameResolver = detectNameResolver;
    }

    public JcacheExpiryCacheResolver(@Nonnull CacheManager manager, boolean detectNameResolver, @Nullable Configuration<?, ?> configuration) {
        super(manager);
        this.detectNameResolver = detectNameResolver;
        this.configuration = configuration;
    }

    @Nonnull
    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public Collection<? extends Cache> resolveCaches(@Nonnull CacheOperationInvocationContext<?> context) {
        CacheResolver cacheResolver = CacheOperationContextUtils.getCacheResolver(beanFactory, context);
        if (cacheResolver != null && !ClassUtils.isAssignableValue(getClass(), cacheResolver)) {
            return cacheResolver.resolveCaches(context);
        }
        CacheManager facadeCacheManager = ObjectUtils.defaultIfNull(CacheOperationContextUtils.getCacheManager(beanFactory, context), super.getCacheManager());
        Assert.isInstanceOf(JCacheCacheManager.class, facadeCacheManager, "Cache manager must be an instanceof " + JCacheCacheManager.class.getCanonicalName());
        javax.cache.CacheManager originCacheManager = ((JCacheCacheManager) facadeCacheManager).getCacheManager();
        Assert.notNull(originCacheManager, "Cache manager for '" + ClassUtils.getQualifiedMethodName(context.getMethod()) + "' must not be null");
        // Prepare caches and configuration
        Collection<String> cacheNames = CacheExpiryDetectionUtils.detectCacheNames(context, beanFactory, detectNameResolver);
        if (CollectionUtils.isEmpty(cacheNames)) {
            return Collections.emptyList();
        }
        MutableConfiguration<?, ?> cloneConfiguration = new MutableConfiguration<>();
        Duration duration = CacheExpiryDetectionUtils.detectCachePeriod(context);
        if (duration != null) {
            if (configuration instanceof MutableConfiguration) {
                BeanUtils.copyProperties(configuration, cloneConfiguration);
            }
            javax.cache.expiry.Duration cacheDuration = new javax.cache.expiry.Duration(TimeUnit.SECONDS, duration.getSeconds());
            cloneConfiguration.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(cacheDuration));
        }
        Collection<Cache> result = new ArrayList<>(cacheNames.size());
        for (String cacheName : cacheNames) {
            if (StringUtils.isBlank(cacheName)) {
                continue;
            }
            Cache facadeCache = null;
            if (duration != null) {
                javax.cache.Cache<?, ?> originCache = originCacheManager.createCache(cacheName, cloneConfiguration);
                if (originCache != null) {
                    facadeCache = new JCacheCache((javax.cache.Cache<Object, Object>) originCache, ((JCacheCacheManager) facadeCacheManager).isAllowNullValues());
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
