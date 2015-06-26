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

package org.codinjutsu.tools.nosql.database.redis;

import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.database.DatabaseVendor;
import org.codinjutsu.tools.nosql.database.redis.model.RedisDatabase;
import org.codinjutsu.tools.nosql.database.redis.model.RedisKeyType;
import org.codinjutsu.tools.nosql.database.redis.model.RedisRecord;
import org.codinjutsu.tools.nosql.database.redis.model.RedisResult;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class RedisManagerTest {

    private Jedis jedis;

    @Test
    public void loadWithEmptyFilter() throws Exception {
        jedis.sadd("books", "eXtreme Programming", "Haskell for Dummies");
        jedis.set("status", "online");
        jedis.lpush("todos", "coffee", "code", "drink", "sleep");
        jedis.zadd("reviews", 12.0d, "writing");
        jedis.zadd("reviews", 14.0d, "reading");
        jedis.zadd("reviews", 15.0d, "maths");

        RedisManager redisManager = new RedisManager();
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setDatabaseVendor(DatabaseVendor.REDIS);
        serverConfiguration.setServerUrls(Arrays.asList("localhost:6379"));

        RedisQuery query = new RedisQuery(null);
        RedisResult result = redisManager.loadRecords(serverConfiguration, new RedisDatabase("0"), query);

        List<RedisRecord> redisRecords = result.getResults();
        assertEquals(4, redisRecords.size());
        RedisRecord redisRecord = redisRecords.get(0);
        assertEquals(RedisKeyType.SET, redisRecord.getKeyType());
        assertEquals("books", redisRecord.getKey());
        redisRecord = redisRecords.get(1);
        assertEquals(RedisKeyType.ZSET, redisRecord.getKeyType());
        assertEquals("reviews", redisRecord.getKey());
        redisRecord = redisRecords.get(2);
        assertEquals(RedisKeyType.LIST, redisRecord.getKeyType());
        assertEquals("todos", redisRecord.getKey());
        redisRecord = redisRecords.get(3);
        assertEquals(RedisKeyType.STRING, redisRecord.getKeyType());
        assertEquals("status", redisRecord.getKey());
    }

    @Before
    public void setUp() throws Exception {
        jedis = new Jedis("localhost", 6379);
        jedis.select(0);
        jedis.flushDB();

    }

    public void tearDown() throws Exception {
        jedis.close();
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("pub-redis-10840.us-east-1-3.3.ec2.garantiadata.com", 10840);
        jedis.auth("nosql4idea");
        jedis.select(0);
        jedis.sadd("codinjutsu:s:books", "eXtrem Programming", "Haskell for Dummies");
        Set<String> books = jedis.smembers("codinjutsu:s:books");
        System.out.println("books = " + books);
    }
}
