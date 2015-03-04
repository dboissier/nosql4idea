/*
 * Copyright (c) 2015 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.nosql.database.redis.model;

import redis.clients.jedis.Tuple;

import java.util.*;

public class RedisResult {

    private final List<RedisRecord> redisRecords = new LinkedList<RedisRecord>();
    private String separator;


    public void addString(String key, String value) {
        redisRecords.add(new RedisRecord<String>(RedisKeyType.STRING, key, value));
    }

    public void addList(String key, List values) {
        redisRecords.add(new RedisRecord<List>(RedisKeyType.LIST, key, values));
    }

    public void addSet(String key, Set values) {
        redisRecords.add(new RedisRecord<Set>(RedisKeyType.SET, key, values));
    }

    public void addHash(String key, Map values) {
        redisRecords.add(new RedisRecord<Map>(RedisKeyType.HASH, key, values));
    }

    public void addSortedSet(String key, Set<Tuple> values) {
        redisRecords.add(new RedisRecord<Set<Tuple>>(RedisKeyType.ZSET, key, values));
    }

    public List<RedisRecord> getResults() {
        return redisRecords;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
