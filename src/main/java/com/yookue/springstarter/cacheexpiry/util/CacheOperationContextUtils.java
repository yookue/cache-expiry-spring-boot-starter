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


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.BasicOperation;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import com.yookue.commonplexus.springutil.util.BeanFactoryWraps;


/**
 * Utilities for {@link org.springframework.cache.CacheManager} and {@link org.springframework.cache.interceptor.CacheResolver}
 *
 * @author David Hsing
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted", "UnusedReturnValue"})
public abstract class CacheOperationContextUtils {
    @Nullable
    public static CacheManager getCacheManager(@Nonnull BeanFactory factory, @Nonnull CacheOperationInvocationContext<?> context) {
        return getCacheManager(factory, context.getOperation());
    }

    @Nullable
    public static <T extends BasicOperation> CacheManager getCacheManager(@Nonnull BeanFactory factory, @Nonnull T operation) {
        if (!(operation instanceof CacheOperation)) {
            return null;
        }
        String beanName = ((CacheOperation) operation).getCacheManager();
        return BeanFactoryWraps.getBean(factory, beanName, CacheManager.class);
    }

    @Nullable
    public static CacheResolver getCacheResolver(@Nonnull BeanFactory factory, @Nonnull CacheOperationInvocationContext<?> context) {
        return getCacheResolver(factory, context.getOperation());
    }

    @Nullable
    public static <T extends BasicOperation> CacheResolver getCacheResolver(@Nonnull BeanFactory factory, @Nonnull T operation) {
        if (!(operation instanceof CacheOperation)) {
            return null;
        }
        String beanName = ((CacheOperation) operation).getCacheResolver();
        return BeanFactoryWraps.getBean(factory, beanName, CacheResolver.class);
    }
}
