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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import com.yookue.commonplexus.springutil.util.CacheUtilsWraps;
import com.yookue.commonplexus.springutil.util.ReflectionUtilsWraps;
import com.yookue.springstarter.cacheexpiry.resolver.ExpiryCacheResolver;
import com.yookue.springstarter.cacheexpiry.util.CacheExpiryDetectionUtils;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;


/**
 * {@link org.springframework.cache.interceptor.CacheResolver} for Redis
 *
 * @author David Hsing
 * @reference "https://blog.csdn.net/liuyanglglg/article/details/102916371"
 * @see org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration
 * @see org.springframework.cache.interceptor.AbstractCacheResolver
 * @see org.springframework.cache.interceptor.CacheAspectSupport
 * @see org.springframework.cache.interceptor.CacheInterceptor
 * @see org.springframework.aop.interceptor.AbstractTraceInterceptor
 * @see org.springframework.aop.ProxyMethodInvocation
 */
@Setter
@SuppressWarnings({"unused", "JavadocDeclaration", "JavadocLinkAsPlainText", "JavadocReference"})
public class RedisExpiryCacheResolver extends SimpleCacheResolver implements BeanFactoryAware, ExpiryCacheResolver {
    @Getter
    private boolean detectNameResolver = false;

    protected BeanFactory beanFactory;

    public RedisExpiryCacheResolver(@Nonnull CacheManager manager) {
        super(manager);
    }

    public RedisExpiryCacheResolver(@Nonnull CacheManager manager, boolean detectNameResolver) {
        super(manager);
        this.detectNameResolver = detectNameResolver;
    }

    @Nonnull
    @Override
    @SneakyThrows
    public Collection<? extends Cache> resolveCaches(@Nonnull CacheOperationInvocationContext<?> context) {
        CacheResolver cacheResolver = CacheUtilsWraps.getCacheResolver(beanFactory, context);
        if (cacheResolver != null && !ClassUtils.isAssignableValue(getClass(), cacheResolver)) {
            return cacheResolver.resolveCaches(context);
        }
        CacheManager cacheManager = ObjectUtils.defaultIfNull(CacheUtilsWraps.getCacheManager(beanFactory, context), super.getCacheManager());
        Assert.isInstanceOf(RedisCacheManager.class, cacheManager, "Cache manager must be an instanceof " + RedisCacheManager.class.getCanonicalName());
        // Prepare caches and configuration
        Collection<String> cacheNames = CacheExpiryDetectionUtils.detectCacheNames(context, beanFactory, detectNameResolver);
        if (CollectionUtils.isEmpty(cacheNames)) {
            return Collections.emptyList();
        }
        RedisCacheConfiguration configuration = ReflectionUtilsWraps.getFieldAs(cacheManager.getClass(), "defaultCacheConfig", true, cacheManager, RedisCacheConfiguration.class);    // $NON-NLS-1$
        configuration = ObjectUtils.defaultIfNull(configuration, RedisCacheConfiguration.defaultCacheConfig());
        Duration duration = CacheExpiryDetectionUtils.detectCachePeriod(context);
        if (duration != null) {
            configuration = configuration.entryTtl(duration);
        }
        Collection<Cache> result = new ArrayList<>(cacheNames.size());
        for (String cacheName : cacheNames) {
            if (StringUtils.isBlank(cacheName)) {
                continue;
            }
            Cache cache = null;
            if (duration != null) {
                cache = (RedisCache) MethodUtils.invokeMethod(cacheManager, true, "createRedisCache", cacheName, configuration);    // $NON-NLS-1$
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
