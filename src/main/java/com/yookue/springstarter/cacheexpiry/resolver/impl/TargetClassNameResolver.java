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
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.util.CollectionUtils;
import com.yookue.commonplexus.javaseutil.constant.CharVariantConst;
import com.yookue.commonplexus.javaseutil.util.StringUtilsWraps;
import com.yookue.commonplexus.springutil.constant.SpringAttributeConst;
import com.yookue.springstarter.cacheexpiry.resolver.CacheNameResolver;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Generates cache names by invocation context and target class
 * <p>
 * Since the {@code key} attribute of {@link org.springframework.cache.annotation.Cacheable} is SpEL, while the {@code cacheNames} is not
 *
 * @author David Hsing
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuppressWarnings("unused")
public class TargetClassNameResolver implements CacheNameResolver {
    private boolean shortClassName = false;
    private boolean methodName = false;
    private boolean indentMethodName = true;
    private boolean resolveSpelName = false;
    private String namePrefix;
    private String nameSuffix;

    public TargetClassNameResolver(boolean shortClassName) {
        this.shortClassName = shortClassName;
    }

    public TargetClassNameResolver(boolean shortClassName, boolean methodName) {
        this.shortClassName = shortClassName;
        this.methodName = methodName;
    }

    public TargetClassNameResolver(boolean shortClassName, boolean methodName, boolean indentMethodName) {
        this.shortClassName = shortClassName;
        this.methodName = methodName;
        this.indentMethodName = indentMethodName;
    }

    public TargetClassNameResolver(boolean shortClassName, boolean methodName, boolean indentMethodName, boolean resolveSpelName) {
        this.shortClassName = shortClassName;
        this.methodName = methodName;
        this.indentMethodName = indentMethodName;
        this.resolveSpelName = resolveSpelName;
    }

    /**
     * Returns generated or resolved cache names for the {@code context}
     *
     * @param context the cache invocation context
     *
     * @return generated or resolved cache names for the {@code context}
     *
     * @reference "https://www.concretepage.com/spring-boot/spring-boot-redis-cache#Cacheable"
     */
    @Override
    @SuppressWarnings({"JavadocDeclaration", "JavadocLinkAsPlainText"})
    public Collection<String> getCacheNames(@Nonnull CacheOperationInvocationContext<?> context) {
        Class<?> targetClazz = AopUtils.getTargetClass(context.getTarget());
        String clazzName = shortClassName ? targetClazz.getSimpleName() : targetClazz.getCanonicalName();
        Set<String> cacheNames = context.getOperation().getCacheNames();
        if (CollectionUtils.isEmpty(cacheNames)) {
            StringBuilder builder = new StringBuilder();
            builder.append(StringUtils.defaultString(namePrefix));
            builder.append(clazzName);
            if (methodName) {
                builder.append(indentMethodName ? CharVariantConst.COLON : CharVariantConst.DOT);
                builder.append(context.getMethod().getName());
            }
            builder.append(StringUtils.defaultString(nameSuffix));
            return Collections.singleton(builder.toString());
        }
        if (!resolveSpelName) {
            return cacheNames;
        }
        return cacheNames.stream().map(element -> {
            String replaced = StringUtilsWraps.replaceAll(element, clazzName, SpringAttributeConst.CACHE_ROOT_TARGET_CLASS, SpringAttributeConst.CACHE_TARGET_CLASS);
            replaced = StringUtilsWraps.replaceAll(replaced, context.getMethod().getName(), SpringAttributeConst.CACHE_ROOT_METHOD_NAME, SpringAttributeConst.CACHE_METHOD_NAME);
            return replaced;
        }).collect(Collectors.toSet());
    }
}
