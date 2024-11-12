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

package com.yookue.springstarter.cacheexpiry;


import java.time.temporal.ChronoUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.yookue.springstarter.cacheexpiry.annotation.CacheExpiry;


@Service
class MockApplicationService {
    @Cacheable(key = "'13800138000'")
    @CacheExpiry(ttl = 3, unit = ChronoUnit.MINUTES)
    @SuppressWarnings("SpringCacheNamesInspection")
    public String generateCaptcha() {
        return RandomStringUtils.randomNumeric(6);
    }
}
