/*
 * Copyright (c) 2013 David Boissier
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

package org.codinjutsu.tools.nosql.mongo.view.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.commons.view.NoSqlTreeNode;
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.NodeDescriptor;
import org.codinjutsu.tools.nosql.mongo.model.MongoResult;
import org.codinjutsu.tools.nosql.mongo.view.nodedescriptor.MongoKeyValueDescriptor;
import org.codinjutsu.tools.nosql.mongo.view.nodedescriptor.MongoResultDescriptor;
import org.codinjutsu.tools.nosql.mongo.view.nodedescriptor.MongoValueDescriptor;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.List;

public class JsonTreeModel extends DefaultTreeModel {


    public JsonTreeModel(MongoResult mongoResult) {
        super(buildJsonTree(mongoResult));
    }


    public static TreeNode buildJsonTree(MongoResult mongoResult) {
        NoSqlTreeNode rootNode = new NoSqlTreeNode(new MongoResultDescriptor(mongoResult.getCollectionName()));

        List<DBObject> mongoObjects = mongoResult.getMongoObjects();
        int i = 0;
        for (DBObject mongoObject : mongoObjects) {
            if (mongoObject instanceof BasicDBList) {
                processDbObject(rootNode, mongoObject);
            } else if (mongoObject instanceof BasicDBObject) {//dead code?
                NoSqlTreeNode currentNode = new NoSqlTreeNode(MongoValueDescriptor.createDescriptor(i++, mongoObject));
                processDbObject(currentNode, mongoObject);
                rootNode.add(currentNode);
            }
        }
        return rootNode;
    }

    public static TreeNode buildJsonTree(DBObject mongoObject) {
        NoSqlTreeNode rootNode = new NoSqlTreeNode(new MongoResultDescriptor());//TODO crappy
        processDbObject(rootNode, mongoObject);
        return rootNode;
    }

    public static void processDbObject(NoSqlTreeNode parentNode, DBObject mongoObject) {
        if (mongoObject instanceof BasicDBList) {
            BasicDBList mongoObjectList = (BasicDBList) mongoObject;
            for (int i = 0; i < mongoObjectList.size(); i++) {
                Object mongoObjectOfList = mongoObjectList.get(i);
                NoSqlTreeNode currentNode = new NoSqlTreeNode(MongoValueDescriptor.createDescriptor(i, mongoObjectOfList));
                if (mongoObjectOfList instanceof DBObject) {
                    processDbObject(currentNode, (DBObject) mongoObjectOfList);
                }
                parentNode.add(currentNode);
            }
        } else if (mongoObject instanceof BasicDBObject) {
            BasicDBObject basicDBObject = (BasicDBObject) mongoObject;
            for (String key : basicDBObject.keySet()) {
                Object value = basicDBObject.get(key);
                NoSqlTreeNode currentNode = new NoSqlTreeNode(MongoKeyValueDescriptor.createDescriptor(key, value));
                if (value instanceof DBObject) {
                    processDbObject(currentNode, (DBObject) value);
                }
                parentNode.add(currentNode);
            }
        }
    }

    public static DBObject buildDBObject(NoSqlTreeNode rootNode) {
        BasicDBObject basicDBObject = new BasicDBObject();
        Enumeration children = rootNode.children();
        while (children.hasMoreElements()) {
            NoSqlTreeNode node = (NoSqlTreeNode) children.nextElement();
            MongoKeyValueDescriptor descriptor = (MongoKeyValueDescriptor) node.getDescriptor();
            Object value = descriptor.getValue();
            if (value instanceof DBObject) {
                if (value instanceof BasicDBList) {
                    basicDBObject.put(descriptor.getKey(), buildDBList(node));
                } else {
                    basicDBObject.put(descriptor.getKey(), buildDBObject(node));
                }
            } else {
                basicDBObject.put(descriptor.getKey(), value);
            }
        }

        return basicDBObject;
    }

    private static DBObject buildDBList(NoSqlTreeNode parentNode) {
        BasicDBList basicDBList = new BasicDBList();
        Enumeration children = parentNode.children();
        while (children.hasMoreElements()) {
            NoSqlTreeNode node = (NoSqlTreeNode) children.nextElement();
            MongoValueDescriptor descriptor = (MongoValueDescriptor) node.getDescriptor();
            Object value = descriptor.getValue();
            if (value instanceof DBObject) {
                if (value instanceof BasicDBList) {
                    basicDBList.add(buildDBList(node));
                } else {
                    basicDBList.add(buildDBObject(node));
                }
            } else {
                basicDBList.add(value);
            }
        }
        return basicDBList;
    }

    public static NoSqlTreeNode findObjectIdNode(NoSqlTreeNode treeNode) {
        NodeDescriptor descriptor = treeNode.getDescriptor();
        if (descriptor instanceof MongoResultDescriptor) { //defensive prog?
            return null;
        }

        if (descriptor instanceof MongoKeyValueDescriptor) {
            MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
            if (StringUtils.equals(keyValueDescriptor.getKey(), "_id")) {
                return treeNode;
            }
        }

        NoSqlTreeNode parentTreeNode = (NoSqlTreeNode) treeNode.getParent();
        if (parentTreeNode.getDescriptor() instanceof MongoValueDescriptor) {
            if (((NoSqlTreeNode) parentTreeNode.getParent()).getDescriptor() instanceof MongoResultDescriptor) {
                //find
            }
        }

        return null;
    }

    public static Object findDocument(NoSqlTreeNode startingNode) {
        if (startingNode.getDescriptor() instanceof MongoValueDescriptor) {
            if (((NoSqlTreeNode) startingNode.getParent()).getDescriptor() instanceof MongoResultDescriptor) {
                return startingNode.getDescriptor().getValue();
            }
        }
        return null;
    }
}
