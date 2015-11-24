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

package org.codinjutsu.tools.nosql.redis;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.redis.model.RedisDatabase;
import redis.clients.jedis.Tuple;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RedisUtils {

    public static String stringifySortedSet(Set<Tuple> sortedSet) {
        List<String> stringifiedTuples = new LinkedList<String>();
        for (Tuple tuple : sortedSet) {
            stringifiedTuples.add(stringifyTuple(tuple));
        }
        return String.format("{%s}", StringUtils.join(stringifiedTuples, ", "));
    }


    public static String stringifySet(Set set) {
        return String.format("{%s}", StringUtils.join(set, ", "));
    }


    public static String stringifyTuple(Tuple tuple) {
        return String.format("(%s, %s)", tuple.getElement(), tuple.getScore());
    }

    public static String buildUrl(ServerConfiguration serverConfiguration, RedisDatabase database) {
        return String.format("-n %s", database.getName());
    }
}
