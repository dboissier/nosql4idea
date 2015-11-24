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

package org.codinjutsu.tools.nosql.couchbase.view;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import org.codinjutsu.tools.nosql.commons.view.NoSqlTreeNode;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseResult;
import org.codinjutsu.tools.nosql.couchbase.view.nodedescriptor.CouchbaseKeyValueDescriptor;
import org.codinjutsu.tools.nosql.couchbase.view.nodedescriptor.CouchbaseResultDescriptor;
import org.codinjutsu.tools.nosql.couchbase.view.nodedescriptor.CouchbaseValueDescriptor;

import javax.swing.tree.DefaultTreeModel;

public class CouchbaseTreeModel extends DefaultTreeModel{
    public CouchbaseTreeModel(CouchbaseResult result) {
        super(buildTree(result));
    }

    public static NoSqlTreeNode buildTree(CouchbaseResult couchbaseResult) {
        NoSqlTreeNode resultTreeNode = new NoSqlTreeNode(new CouchbaseResultDescriptor(couchbaseResult.getName()));
        for (JsonObject record: couchbaseResult.getRecords()) {
            processRecord(resultTreeNode, record);
        }

        return resultTreeNode;
    }

    private static void processRecord(NoSqlTreeNode parentNode, JsonObject record) {
        for (String key: record.getNames()) {
            Object value = record.get(key);
            NoSqlTreeNode currentNode = new NoSqlTreeNode(CouchbaseKeyValueDescriptor.createDescriptor(key, value));
            if (value instanceof JsonArray) {
                processRecordListValues(currentNode, (JsonArray) value);
            } else if (value instanceof JsonObject) {
                processRecord(currentNode, (JsonObject) value);
            }

            parentNode.add(currentNode);
        }
    }

    private static void processRecordListValues(NoSqlTreeNode parentNode, JsonArray values) {
        int index = 0;
        for (Object value: values) {
            NoSqlTreeNode currentValueNode = new NoSqlTreeNode(CouchbaseValueDescriptor.createDescriptor(index++, value));
            if (value instanceof JsonArray) {
                processRecordListValues(currentValueNode, (JsonArray) value);
            } else if (value instanceof JsonObject) {
                processRecord(currentValueNode, (JsonObject) value);
            }
            parentNode.add(currentValueNode);
        }
    }
}
