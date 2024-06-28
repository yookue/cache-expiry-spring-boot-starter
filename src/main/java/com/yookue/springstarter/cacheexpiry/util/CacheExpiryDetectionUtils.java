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

package com.yookue.springstarter.cacheexpiry.util;


import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import com.yookue.commonplexus.springutil.util.BeanFactoryWraps;
import com.yookue.springstarter.cacheexpiry.annotation.CacheExpiry;
import com.yookue.springstarter.cacheexpiry.annotation.CacheExpiryConfig;
import com.yookue.springstarter.cacheexpiry.resolver.CacheNameResolver;


/**
 * Utilities for cache expiry annotations
 *
 * @author David Hsing
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted", "UnusedReturnValue"})
public abstract class CacheExpiryDetectionUtils {
    @Nullable
    public static Collection<String> detectCacheNames(@Nonnull CacheOperationInvocationContext<?> context, @Nonnull BeanFactory factory, boolean detectResolver) {
        Collection<String> cacheNames = context.getOperation().getCacheNames();
        if (!CollectionUtils.isEmpty(cacheNames)) {
            return cacheNames;
        }
        String nameResolver = null;
        CacheExpiry methodAnnotation = AnnotationUtils.getAnnotation(context.getMethod(), CacheExpiry.class);
        if (methodAnnotation != null) {
            nameResolver = methodAnnotation.nameResolver();
        }
        if (StringUtils.isBlank(nameResolver)) {
            CacheExpiryConfig typeAnnotation = AnnotationUtils.getAnnotation(AopUtils.getTargetClass(context.getTarget()), CacheExpiryConfig.class);
            if (typeAnnotation != null) {
                nameResolver = typeAnnotation.nameResolver();
            }
        }
        if (StringUtils.isBlank(nameResolver)) {
            if (detectResolver) {
                CacheNameResolver resolver = BeanFactoryWraps.getBean(factory, CacheNameResolver.class);
                if (resolver != null) {
                    return resolver.getCacheNames(context);
                }
            }
            return null;
        } else {
            CacheNameResolver resolver = BeanFactoryWraps.getBean(factory, nameResolver, CacheNameResolver.class);
            return (resolver == null) ? null : resolver.getCacheNames(context);
        }
    }

    @Nullable
    public static Duration detectCachePeriod(@Nonnull CacheOperationInvocationContext<?> context) {
        return detectCachePeriod(context.getMethod());
    }

    @Nullable
    public static Duration detectCachePeriod(@Nonnull Method method) {
        CacheExpiry annotation = AnnotationUtils.getAnnotation(method, CacheExpiry.class);
        if (annotation == null || annotation.ttl() <= 0L || annotation.unit() == ChronoUnit.FOREVER) {
            return null;
        }
        return annotation.unit().getDuration().multipliedBy(annotation.ttl());
    }
}
