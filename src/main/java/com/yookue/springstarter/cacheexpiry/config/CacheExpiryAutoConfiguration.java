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

package com.yookue.springstarter.cacheexpiry.config;


import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.Order;
import com.yookue.commonplexus.springutil.enumeration.CacheManagerType;
import com.yookue.springstarter.cacheexpiry.processor.CacheExpiryResolverProcessor;
import com.yookue.springstarter.cacheexpiry.property.CacheExpiryProperties;
import com.yookue.springstarter.cacheexpiry.resolver.CacheNameResolver;
import com.yookue.springstarter.cacheexpiry.resolver.impl.TargetClassNameResolver;
import jakarta.annotation.Nonnull;


/**
 * Configuration for cache expiry
 *
 * @author David Hsing
 * @see org.springframework.cache.annotation.AbstractCachingConfiguration
 * @see org.springframework.cache.annotation.ProxyCachingConfiguration
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = CacheExpiryAutoConfiguration.PROPERTIES_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(value = CacheManager.class)
@ConditionalOnBean(value = CacheAspectSupport.class)
@AutoConfigureAfter(value = CacheAutoConfiguration.class)
@Import(value = {CacheExpiryAutoConfiguration.Entry.class, CacheExpiryAutoConfiguration.Caffeine.class, CacheExpiryAutoConfiguration.Jcache.class, CacheExpiryAutoConfiguration.Redis.class})
public class CacheExpiryAutoConfiguration {
    public static final String PROPERTIES_PREFIX = "spring.cache-expiry";    // $NON-NLS-1$
    public static final String CACHE_MANAGER = "cacheExpiryCacheManager";    // $NON-NLS-1$
    public static final String CACHE_RESOLVER = "cacheExpiryCacheResolver";    // $NON-NLS-1$


    @Order(value = 0)
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    static class Entry {
        @Bean
        @ConditionalOnMissingBean
        @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
        public CacheExpiryProperties cacheExpiryProperties() {
            return new CacheExpiryProperties();
        }

        @Bean
        @ConditionalOnProperty(prefix = PROPERTIES_PREFIX + ".cache-name-resolver", name = "enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean
        public CacheNameResolver targetClassNameResolver(@Nonnull CacheExpiryProperties properties) {
            CacheExpiryProperties.CacheNameResolver props = properties.getCacheNameResolver();
            TargetClassNameResolver result = new TargetClassNameResolver();
            result.setShortClassName(BooleanUtils.isTrue(props.getShortClassName()));
            result.setIndentMethodName(BooleanUtils.isTrue(props.getIndentMethodName()));
            result.setResolveSpelName(BooleanUtils.isTrue(props.getResolveSpelName()));
            result.setNamePrefix(props.getNamePrefix());
            result.setNameSuffix(props.getNameSuffix());
            return result;
        }
    }


    @Order(value = 1)
    @ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "caffeine", matchIfMissing = true)
    @ConditionalOnClass(name = "com.github.benmanes.caffeine.cache.Caffeine")
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    static class Caffeine {
        @Bean
        @ConditionalOnMissingBean
        public CacheExpiryResolverProcessor cacheExpiryResolverProcessor(@Nonnull CacheExpiryProperties properties) {
            CacheExpiryResolverProcessor result = new CacheExpiryResolverProcessor(properties, CacheManagerType.CAFFEINE);
            Optional.ofNullable(properties.getCacheResolver().getProcessorOrder()).ifPresent(result::setOrder);
            return result;
        }
    }


    @Order(value = 2)
    @ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "jcache", matchIfMissing = true)
    @ConditionalOnClass(name = "javax.cache.CacheManager")
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    static class Jcache {
        @Bean
        @ConditionalOnMissingBean
        public CacheExpiryResolverProcessor cacheExpiryResolverProcessor(@Nonnull CacheExpiryProperties properties) {
            CacheExpiryResolverProcessor result = new CacheExpiryResolverProcessor(properties, CacheManagerType.JCACHE);
            Optional.ofNullable(properties.getCacheResolver().getProcessorOrder()).ifPresent(result::setOrder);
            return result;
        }
    }


    @Order(value = 3)
    @ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "redis", matchIfMissing = true)
    @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisOperations")
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    static class Redis {
        @Bean
        @ConditionalOnMissingBean
        public CacheExpiryResolverProcessor cacheExpiryResolverProcessor(@Nonnull CacheExpiryProperties properties) {
            CacheExpiryResolverProcessor result = new CacheExpiryResolverProcessor(properties, CacheManagerType.REDIS);
            Optional.ofNullable(properties.getCacheResolver().getProcessorOrder()).ifPresent(result::setOrder);
            return result;
        }
    }
}
