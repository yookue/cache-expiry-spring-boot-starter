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

package com.yookue.springstarter.cacheexpiry.processor;


import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import com.yookue.commonplexus.javaseutil.constant.AssertMessageConst;
import com.yookue.commonplexus.javaseutil.exception.UnsupportedClassException;
import com.yookue.commonplexus.springutil.enumeration.CacheManagerType;
import com.yookue.commonplexus.springutil.util.BeanFactoryWraps;
import com.yookue.commonplexus.springutil.util.ClassUtilsWraps;
import com.yookue.springstarter.cacheexpiry.config.CacheExpiryAutoConfiguration;
import com.yookue.springstarter.cacheexpiry.property.CacheExpiryProperties;
import com.yookue.springstarter.cacheexpiry.resolver.ExpiryCacheResolver;
import com.yookue.springstarter.cacheexpiry.resolver.impl.CaffeineExpiryCacheResolver;
import com.yookue.springstarter.cacheexpiry.resolver.impl.JcacheExpiryCacheResolver;
import com.yookue.springstarter.cacheexpiry.resolver.impl.RedisExpiryCacheResolver;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} for registering resolver bean
 *
 * @author David Hsing
 */
@RequiredArgsConstructor
public class CacheExpiryResolverProcessor implements BeanFactoryAware, BeanPostProcessor, InitializingBean, Ordered {
    private final CacheExpiryProperties expiryProperties;
    private final CacheManagerType managerType;

    @Getter
    @Setter
    private int order = 0;

    private CacheInterceptor cacheInterceptor;
    private CacheManager cacheManager;
    private Class<?> managerClass;
    private boolean propManagerExist = false;
    private boolean codeManagerExist = false;
    private boolean beanRegistered = false;

    @Setter
    protected BeanFactory beanFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(managerType, AssertMessageConst.NOT_NULL);
        managerClass = ClassUtils.forName(managerType.getValue(), null);
        CacheExpiryProperties.CacheManager props = expiryProperties.getCacheManager();
        BeanDefinition propDefinition = StringUtils.isBlank(props.getCacheManagerName()) ? null : BeanFactoryWraps.getBeanDefinitionQuietly(beanFactory, props.getCacheManagerName());
        BeanDefinition codeDefinition = BeanFactoryWraps.getBeanDefinitionQuietly(beanFactory, CacheExpiryAutoConfiguration.CACHE_MANAGER);
        propManagerExist = (propDefinition != null) && ClassUtilsWraps.isAssignable(managerType.getValue(), propDefinition.getBeanClassName());
        codeManagerExist = (codeDefinition != null) && ClassUtilsWraps.isAssignable(managerType.getValue(), codeDefinition.getBeanClassName());
        if (!propManagerExist && !codeManagerExist && BooleanUtils.isFalse(props.getDetectCacheManager())) {
            throw new IllegalStateException("None cache manager found! Property 'cache-manager-name' is not specified, and 'detect-cache-manager' is also disabled.");
        }
    }

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (beanRegistered) {
            return bean;
        }
        if (cacheInterceptor == null && bean instanceof CacheInterceptor) {
            cacheInterceptor = (CacheInterceptor) bean;
        }
        if (cacheManager == null && bean instanceof CacheManager && ClassUtils.isAssignableValue(managerClass, bean)) {
            CacheExpiryProperties.CacheManager props = expiryProperties.getCacheManager();
            if (propManagerExist && StringUtils.equals(beanName, props.getCacheManagerName())) {
                cacheManager = (CacheManager) bean;
            }
            if (!propManagerExist && codeManagerExist && StringUtils.equals(beanName, CacheExpiryAutoConfiguration.CACHE_MANAGER)) {
                cacheManager = (CacheManager) bean;
            }
            if (!propManagerExist && !codeManagerExist && BooleanUtils.isNotFalse(props.getDetectCacheManager())) {
                cacheManager = (CacheManager) bean;
            }
        }
        if (cacheInterceptor != null && cacheManager != null) {
            ExpiryCacheResolver resolver = detectCacheResolver();
            beanRegistered = BeanFactoryWraps.registerSingletonBean(beanFactory, CacheExpiryAutoConfiguration.CACHE_RESOLVER, resolver);
            CacheExpiryProperties.CacheInterceptor interceptorProps = expiryProperties.getCacheInterceptor();
            if (BooleanUtils.isNotFalse(interceptorProps.getInjectCacheManager())) {
                cacheInterceptor.setCacheResolver(resolver);
            }
        }
        return bean;
    }

    @Nonnull
    private ExpiryCacheResolver detectCacheResolver() {
        CacheExpiryProperties.CacheResolver resolverProps = expiryProperties.getCacheResolver();
        ExpiryCacheResolver resolver = switch (managerType) {
            case CAFFEINE -> new CaffeineExpiryCacheResolver(cacheManager, BooleanUtils.isNotFalse(resolverProps.getDetectCacheNameResolver()));
            case JCACHE -> new JcacheExpiryCacheResolver(cacheManager, BooleanUtils.isNotFalse(resolverProps.getDetectCacheNameResolver()));
            case REDIS -> new RedisExpiryCacheResolver(cacheManager, BooleanUtils.isNotFalse(resolverProps.getDetectCacheNameResolver()));
            default -> throw new UnsupportedClassException("Unsupported cache manager type: " + managerType.name());    // $NON-NLS-1$
        };
        resolver.setBeanFactory(beanFactory);
        return resolver;
    }

}
