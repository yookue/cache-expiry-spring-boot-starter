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

package com.yookue.springstarter.cacheexpiry.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;
import org.apache.commons.lang3.StringUtils;


/**
 * Annotation for setting cache expiry period
 * <p>
 * Way 1: Using with {@code cacheResolver} attribute together
 *
 * <pre><code>
 *     &#64;CacheConfig(cacheResolver = RedisExpiryCacheConfiguration.CACHE_RESOLVER)
 * </code></pre>
 * <p>
 * Way 2: Setting resolver above as the primary bean in the configuration (the default way)
 *
 * <pre><code>
 *     spring.cache-expiry.primary-resolver = true
 * </code></pre>
 *
 * @author David Hsing
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CacheExpiry {
    /**
     * Returns the time amount for the cache
     *
     * @return the time amount for the cache
     */
    long ttl();

    /**
     * Returns the time unit for the cache
     *
     * @return the time unit for the cache
     */
    ChronoUnit unit() default ChronoUnit.SECONDS;

    /**
     * Returns the cache names if not specified on method
     * <p>
     * Specifies the bean name which extended from {@link com.yookue.springstarter.cacheexpiry.resolver.CacheNameResolver}
     *
     * @return the cache names if not specified on method
     */
    String nameResolver() default StringUtils.EMPTY;
}
