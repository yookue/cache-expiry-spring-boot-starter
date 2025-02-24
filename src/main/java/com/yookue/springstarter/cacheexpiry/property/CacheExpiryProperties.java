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

package com.yookue.springstarter.cacheexpiry.property;


import java.io.Serializable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import com.yookue.springstarter.cacheexpiry.config.CacheExpiryAutoConfiguration;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * Properties for cache expiry
 *
 * @author David Hsing
 */
@ConfigurationProperties(prefix = CacheExpiryAutoConfiguration.PROPERTIES_PREFIX)
@Getter
@Setter
@ToString
public class CacheExpiryProperties implements Serializable {
    /**
     * Indicates whether to enable this starter or not
     * <p>
     * Default is {@code true}
     */
    private Boolean enabled = true;

    /**
     * Cache interceptor attributes
     */
    private final CacheInterceptor cacheInterceptor = new CacheInterceptor();

    /**
     * Cache manager attributes
     */
    private final CacheManager cacheManager = new CacheManager();

    /**
     * Cache resolver attributes
     */
    private final CacheResolver cacheResolver = new CacheResolver();

    /**
     * Cache name resolver attributes
     */
    private final CacheNameResolver cacheNameResolver = new CacheNameResolver();


    /**
     * Properties for cache interceptor
     *
     * @author David Hsing
     * @see org.springframework.cache.interceptor.CacheInterceptor
     */
    @Getter
    @Setter
    @ToString
    public static class CacheInterceptor implements Serializable {
        /**
         * Indicates whether to inject cache interceptor with expiry resolver or not
         * <p>
         * Default is {@code true}
         */
        private Boolean injectCacheManager = true;
    }


    /**
     * Properties for cache manager
     *
     * @author David Hsing
     * @see org.springframework.cache.CacheManager
     */
    @Getter
    @Setter
    @ToString
    public static class CacheManager implements Serializable {
        /**
         * The bean name of cache manager to associate with
         * <p>
         * If this is not blank, then {@code detectCacheManager} is disabled
         * <p>
         * Note : To configure the cache manager that associates with cache resolver, this stater finds cache manager bean follow these ways:
         * <ol>
         *     <li>Defines a {@link org.springframework.cache.CacheManager} bean and records it's bean name to {@code cacheManagerName}</li>
         *     <li>Defines a {@link org.springframework.cache.CacheManager} bean and specifies it's bean name with {@code "cacheExpiryCacheManager"}, leave {@code cacheManagerName} blank</li>
         *     <li>Auto detection. This starter finds the first priority bean of {@link org.springframework.cache.CacheManager}</li>
         * </ol>
         */
        private String cacheManagerName;

        /**
         * Whether to detect cache manager or not
         * <p>
         * Default is {@code true}
         */
        private Boolean detectCacheManager = true;
    }


    /**
     * Properties for cache resolver
     *
     * @author David Hsing
     * @see com.yookue.springstarter.cacheexpiry.resolver.ExpiryCacheResolver
     */
    @Getter
    @Setter
    @ToString
    public static class CacheResolver implements Serializable {
        /**
         * Whether to detect the cache name resolver
         * <p>
         * Default is {@code true}
         */
        private Boolean detectCacheNameResolver = true;

        /**
         * The priority order of processor that processes cache resolver
         * <p>
         * Default is {@code Ordered.LOWEST_PRECEDENCE - 1000}
         */
        private Integer processorOrder = Ordered.LOWEST_PRECEDENCE - 1000;
    }


    /**
     * Properties for cache name resolver
     *
     * @author David Hsing
     * @see com.yookue.springstarter.cacheexpiry.resolver.CacheNameResolver
     */
    @Getter
    @Setter
    @ToString
    public static class CacheNameResolver implements Serializable {
        /**
         * Indicates whether to enable this resolver or not
         * <p>
         * Default is {@code true}
         */
        private Boolean enabled = true;

        /**
         * Whether to use short class name in the generated cache name or not
         */
        private Boolean shortClassName;

        /**
         * Whether to use method name in the generated cache name or not
         */
        private Boolean methodName;

        /**
         * Whether to indent method name in the generated cache name or not
         * <p>
         * For example, the cache name will be {@code DemoService:demoMethodName} if this is {@code true}, otherwise, it will be {@code DemoService.demoMethodName}
         * <p>
         * Default is {@code true}
         */
        private Boolean indentMethodName = true;

        /**
         * Whether to resolve SpEL name in the generated cache name or not
         */
        private Boolean resolveSpelName;

        /**
         * The prefix for the generated cache name
         */
        private String namePrefix;

        /**
         * The suffix for the generated cache name
         */
        private String nameSuffix;
    }
}
