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

package org.springframework.boot.autoconfigure.cache;


import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.data.redis.cache.RedisCacheConfiguration;


/**
 * Utilities for redis cache
 *
 * @author David Hsing
 * @see org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted", "UnusedReturnValue"})
public abstract class RedisCacheConfigurationUtils {
    @Nonnull
    public static RedisCacheConfiguration createConfiguration(@Nonnull CacheProperties properties) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return createConfiguration(properties, null);
    }

    @Nonnull
    public static RedisCacheConfiguration createConfiguration(@Nonnull CacheProperties properties, @Nullable ClassLoader loader) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration configuration = new org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration();
        return (RedisCacheConfiguration) MethodUtils.invokeMethod(configuration, true, "createConfiguration", properties, loader);    // $NON-NLS-1$
    }
}
