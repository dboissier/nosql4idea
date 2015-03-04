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

package org.codinjutsu.tools.nosql.view.model;

import org.codinjutsu.tools.nosql.database.redis.model.RedisKeyType;
import org.codinjutsu.tools.nosql.database.redis.model.RedisRecord;
import org.codinjutsu.tools.nosql.database.redis.model.RedisResult;
import org.codinjutsu.tools.nosql.view.nodedescriptor.redis.RedisKeyValueDescriptor;
import org.codinjutsu.tools.nosql.view.nodedescriptor.redis.RedisResultDescriptor;
import org.codinjutsu.tools.nosql.view.nodedescriptor.redis.RedisValueDescriptor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisTreeModel {
    public static NoSqlTreeNode buildTree(RedisResult redisResult) {
        NoSqlTreeNode rootNode = new NoSqlTreeNode(new RedisResultDescriptor());

        List<RedisRecord> redisRecords = redisResult.getResults();
        for (RedisRecord redisRecord : redisRecords) {
            processRecord(rootNode, redisRecord);
        }

        return rootNode;
    }

    private static void processRecord(NoSqlTreeNode rootNode, RedisRecord redisRecord) {
        RedisKeyType keyType = redisRecord.getKeyType();
        NoSqlTreeNode treeNode = new NoSqlTreeNode(RedisKeyValueDescriptor.createDescriptor(redisRecord.getKeyType(), redisRecord.getKey(), redisRecord.getValue()));
        if (RedisKeyType.LIST.equals(keyType)) {
            List<String> valuesFromList = (List<String>) redisRecord.getValue();
            for (int index = 0; index < valuesFromList.size(); index++) {
                String value = valuesFromList.get(index);
                treeNode.add(new NoSqlTreeNode(RedisValueDescriptor.createDescriptor(index, value)));
            }
        } else if (RedisKeyType.SET.equals(keyType) || RedisKeyType.ZSET.equals(keyType)) {
            Set valuesFromSet = (Set) redisRecord.getValue();
            for (Object value : valuesFromSet) {
                treeNode.add(new NoSqlTreeNode(RedisValueDescriptor.createUnindexedDescriptor(value)));
            }
        } else if (RedisKeyType.HASH.equals(keyType)) {
            Map<String, String> valuesFromMap = (Map<String, String>) redisRecord.getValue();
            for (Map.Entry<String, String> entry : valuesFromMap.entrySet()) {
                treeNode.add(new NoSqlTreeNode(RedisKeyValueDescriptor.createDescriptor(RedisKeyType.STRING, entry.getKey(), entry.getValue())));
            }
        }
        rootNode.add(treeNode);

    }
}
