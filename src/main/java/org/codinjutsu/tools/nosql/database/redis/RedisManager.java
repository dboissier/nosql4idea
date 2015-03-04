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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.database.DatabaseClient;
import org.codinjutsu.tools.nosql.database.DatabaseServer;
import org.codinjutsu.tools.nosql.database.NoSqlDatabase;
import org.codinjutsu.tools.nosql.database.mongo.model.MongoCollection;
import org.codinjutsu.tools.nosql.database.mongo.model.MongoDatabase;
import org.codinjutsu.tools.nosql.database.redis.model.RedisDatabase;
import org.codinjutsu.tools.nosql.database.redis.model.RedisKeyType;
import org.codinjutsu.tools.nosql.database.redis.model.RedisResult;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisManager implements DatabaseClient {

    public static RedisManager getInstance(Project project) {
        return ServiceManager.getService(project, RedisManager.class);
    }


    @Override
    public void connect(ServerConfiguration serverConfiguration) {

    }

    @Override
    public void loadServer(DatabaseServer databaseServer) {
        Jedis jedis = createJedis(databaseServer.getConfiguration());
        List<String> databases = jedis.configGet("databases");
        assert databases.size() > 0;
        databaseServer.setDatabases(Arrays.<NoSqlDatabase>asList(new RedisDatabase("0")));
    }

    @Override
    public void dropCollection(ServerConfiguration configuration, MongoCollection selectedCollection) {

    }

    @Override
    public void dropDatabase(ServerConfiguration configuration, MongoDatabase selectedDatabase) {

    }

    public RedisResult loadRecords(ServerConfiguration serverConfiguration, RedisQuery query) {
        Jedis jedis = createJedis(serverConfiguration);
        Set<String> keys = jedis.keys("*");
        RedisResult redisResult = new RedisResult();
        List<String> databases = jedis.configGet("databases");
        System.out.println("databases = " + databases);
        jedis.select(0);

        for (String key : keys) {
            System.out.println("key = " + key);
            RedisKeyType keyType = RedisKeyType.getKeyType(jedis.type(key));
            if (RedisKeyType.LIST.equals(keyType)) {
                List<String> values = jedis.lrange(key, 0, -1);
                redisResult.addList(key, values);
            } else if (RedisKeyType.SET.equals(keyType)) {
                Set<String> values = jedis.smembers(key);
                redisResult.addSet(key, values);
            } else if (RedisKeyType.HASH.equals(keyType)) {
                Map<String, String> values = jedis.hgetAll(key);
                redisResult.addHash(key, values);
            } else if (RedisKeyType.ZSET.equals(keyType)) {
                Set<Tuple> valuesWithScores = jedis.zrangeByScoreWithScores(key, "-inf", "+inf");
                redisResult.addSortedSet(key, valuesWithScores);
            } else if (RedisKeyType.STRING.equals(keyType)) {
                String value = jedis.get(key);
                redisResult.addString(key, value);
            }
        }
        return redisResult;
    }

    private Jedis createJedis(ServerConfiguration serverConfiguration) {
        String[] url_port = serverConfiguration.getServerUrls().get(0).split(":");
        return new Jedis(url_port[0], Integer.valueOf(url_port[1]));
    }
}
