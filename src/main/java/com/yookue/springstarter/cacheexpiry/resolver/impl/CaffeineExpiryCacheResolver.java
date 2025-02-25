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
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yookue.commonplexus.springutil.util.CacheUtilsWraps;
import com.yookue.commonplexus.springutil.util.ReflectionUtilsWraps;
import com.yookue.springstarter.cacheexpiry.resolver.ExpiryCacheResolver;
import com.yookue.springstarter.cacheexpiry.util.CacheExpiryDetectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;


/**
 * {@link org.springframework.cache.interceptor.CacheResolver} for Caffeine
 *
 * @author David Hsing
 * @reference "https://www.baeldung.com/java-caching-caffeine"
 * @see org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration
 */
@Setter
@SuppressWarnings({"unused", "JavadocDeclaration", "JavadocLinkAsPlainText", "JavadocReference"})
public class CaffeineExpiryCacheResolver extends SimpleCacheResolver implements BeanFactoryAware, ExpiryCacheResolver {
    @Getter
    private boolean detectNameResolver = false;

    protected BeanFactory beanFactory;

    public CaffeineExpiryCacheResolver(@Nonnull CacheManager manager) {
        super(manager);
    }

    public CaffeineExpiryCacheResolver(@Nonnull CacheManager manager, boolean detectNameResolver) {
        super(manager);
        this.detectNameResolver = detectNameResolver;
    }

    @Nonnull
    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public Collection<? extends Cache> resolveCaches(@Nonnull CacheOperationInvocationContext<?> context) {
        CacheResolver cacheResolver = CacheUtilsWraps.getCacheResolver(beanFactory, context);
        if (cacheResolver != null && !ClassUtils.isAssignableValue(getClass(), cacheResolver)) {
            return cacheResolver.resolveCaches(context);
        }
        CacheManager cacheManager = ObjectUtils.defaultIfNull(CacheUtilsWraps.getCacheManager(beanFactory, context), super.getCacheManager());
        Assert.isInstanceOf(CaffeineCacheManager.class, cacheManager, "Cache manager must be an instanceof " + CaffeineCacheManager.class.getCanonicalName());
        // Prepare caches and configuration
        Collection<String> cacheNames = CacheExpiryDetectionUtils.detectCacheNames(context, beanFactory, detectNameResolver);
        if (CollectionUtils.isEmpty(cacheNames)) {
            return Collections.emptyList();
        }
        Caffeine<Object, Object> cacheBuilder = ReflectionUtilsWraps.getFieldAs(cacheManager.getClass(), "cacheBuilder", true, cacheManager, Caffeine.class);    // $NON-NLS-1$
        cacheBuilder = ObjectUtils.defaultIfNull(cacheBuilder, Caffeine.newBuilder());
        Duration duration = CacheExpiryDetectionUtils.detectCachePeriod(context);
        if (duration != null) {
            cacheBuilder = cacheBuilder.expireAfterWrite(duration);
        }
        com.github.benmanes.caffeine.cache.Cache<Object, Object> configuration = cacheBuilder.build();
        Collection<Cache> result = new ArrayList<>(cacheNames.size());
        for (String cacheName : cacheNames) {
            if (StringUtils.isBlank(cacheName)) {
                continue;
            }
            Cache cache = null;
            if (duration != null) {
                cache = (Cache) MethodUtils.invokeMethod(cacheManager, true, "adaptCaffeineCache", cacheName, configuration);    // $NON-NLS-1$
            }
            if (cache == null) {
                cache = cacheManager.getCache(cacheName);
            }
            if (cache == null) {
                throw new IllegalStateException("Cannot find/create cache '" + cacheName + "' with operation " + context.getOperation());
            }
            result.add(cache);
        }
        return result;
    }
}
