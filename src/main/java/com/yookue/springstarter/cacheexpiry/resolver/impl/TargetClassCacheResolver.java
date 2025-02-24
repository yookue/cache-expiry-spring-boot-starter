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


import java.util.Collection;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import jakarta.annotation.Nonnull;
import lombok.Getter;


/**
 * Generates cache by invocation context and target class
 * <p>
 * Since the {@code key} attribute of {@link org.springframework.cache.annotation.Cacheable} is SpEL, while the {@code cacheNames} is not
 *
 * @author David Hsing
 * @see org.springframework.cache.interceptor.SimpleCacheResolver
 */
@Getter
@SuppressWarnings("unused")
public class TargetClassCacheResolver extends AbstractCacheResolver {
    private final TargetClassNameResolver nameResolver;

    public TargetClassCacheResolver(@Nonnull CacheManager manager) {
        super(manager);
        nameResolver = new TargetClassNameResolver();
    }

    public TargetClassCacheResolver(@Nonnull CacheManager manager, boolean shortClassName) {
        super(manager);
        nameResolver = new TargetClassNameResolver(shortClassName);
    }

    public TargetClassCacheResolver(@Nonnull CacheManager manager, boolean shortClassName, boolean methodName) {
        super(manager);
        nameResolver = new TargetClassNameResolver(shortClassName, methodName);
    }

    public TargetClassCacheResolver(@Nonnull CacheManager manager, boolean shortClassName, boolean methodName, boolean indentMethodName) {
        super(manager);
        nameResolver = new TargetClassNameResolver(shortClassName, methodName, indentMethodName);
    }

    public TargetClassCacheResolver(@Nonnull CacheManager manager, boolean shortClassName, boolean methodName, boolean indentMethodName, boolean resolveSpelName) {
        super(manager);
        nameResolver = new TargetClassNameResolver(shortClassName, methodName, indentMethodName, resolveSpelName);
    }

    public TargetClassCacheResolver(@Nonnull CacheManager manager, boolean shortClassName, boolean methodName, boolean indentMethodName, boolean resolveSpelName, String namePrefix, String nameSuffix) {
        super(manager);
        nameResolver = new TargetClassNameResolver(shortClassName, methodName, indentMethodName, resolveSpelName, namePrefix, nameSuffix);
    }

    @Override
    protected Collection<String> getCacheNames(@Nonnull CacheOperationInvocationContext<?> context) {
        return nameResolver.getCacheNames(context);
    }
}
