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

package org.codinjutsu.tools.nosql.mongo.view.model;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.codinjutsu.tools.nosql.commons.view.NoSqlTreeNode;
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.NodeDescriptor;
import org.codinjutsu.tools.nosql.mongo.model.MongoResult;
import org.codinjutsu.tools.nosql.mongo.view.nodedescriptor.MongoKeyValueDescriptor;
import org.codinjutsu.tools.nosql.mongo.view.nodedescriptor.MongoResultDescriptor;
import org.codinjutsu.tools.nosql.mongo.view.nodedescriptor.MongoValueDescriptor;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class JsonTreeModel extends DefaultTreeModel {


    public JsonTreeModel(MongoResult mongoResult) {
        super(buildJsonTree(mongoResult));
    }


    public static TreeNode buildJsonTree(MongoResult mongoResult) {
        NoSqlTreeNode rootNode = new NoSqlTreeNode(new MongoResultDescriptor(mongoResult.getCollectionName()));

        List<Document> mongoObjects = mongoResult.getMongoObjects();
        int i = 0;
        for (Document mongoObject : mongoObjects) {
            NoSqlTreeNode currentNode = new NoSqlTreeNode(MongoValueDescriptor.createDescriptor(i++, mongoObject));
            processDocument(currentNode, mongoObject);
            rootNode.add(currentNode);

            // todo: do we really need to support array here?
            // old code
//            if (mongoObject instanceof BasicDBList) {
//                processDocument(rootNode, mongoObject);
//            } else if (mongoObject instanceof BasicDBObject) {//dead code?
//                NoSqlTreeNode currentNode = new NoSqlTreeNode(MongoValueDescriptor.createDescriptor(i++, mongoObject));
//                processDocument(currentNode, mongoObject);
//                rootNode.add(currentNode);
//            }
        }
        return rootNode;
    }

    public static TreeNode buildJsonTree(Object mongoObject) {
        NoSqlTreeNode rootNode = new NoSqlTreeNode(new MongoResultDescriptor());//TODO crappy
        processDocument(rootNode, mongoObject);
        return rootNode;
    }

    private static void processDocument(NoSqlTreeNode parentNode, List documentList) {
        for (int i = 0; i < documentList.size(); i++) {
            Object mongoObjectOfList = documentList.get(i);
            NoSqlTreeNode currentNode = new NoSqlTreeNode(MongoValueDescriptor.createDescriptor(i, mongoObjectOfList));
            if (mongoObjectOfList instanceof Document) {
                processDocument(currentNode, (Document) mongoObjectOfList);
            } else if (mongoObjectOfList instanceof List) {
                processDocument(currentNode, (List) mongoObjectOfList);
            }

            parentNode.add(currentNode);
        }
    }

    public static void processDocument(NoSqlTreeNode parentNode, Object object) {
        if (object instanceof Document) {
            processDocument(parentNode, (Document) object);
        } else if (object instanceof List) {
            processDocument(parentNode, (List) object);
        } else {
            throw new RuntimeException("Unsupported object to process");
        }
    }

    public static void processDocument(NoSqlTreeNode parentNode, Document document) {
        for (String key : document.keySet()) {
            Object value = document.get(key);
            NoSqlTreeNode currentNode = new NoSqlTreeNode(MongoKeyValueDescriptor.createDescriptor(key, value));

            if (value instanceof Document) {
                processDocument(currentNode, (Document) value);
            } else if (value instanceof List) {
                processDocument(currentNode, (List) value);
            }
            parentNode.add(currentNode);
        }
    }

    public static Document buildDBDocument(NoSqlTreeNode rootNode) {
        final Document document = new Document();
        Enumeration children = rootNode.children();
        while (children.hasMoreElements()) {
            NoSqlTreeNode node = (NoSqlTreeNode) children.nextElement();
            MongoKeyValueDescriptor descriptor = (MongoKeyValueDescriptor) node.getDescriptor();
            Object value = descriptor.getValue();

            if (value instanceof Document) {
                document.put(descriptor.getKey(), buildDBDocument(node));
            } else if (value instanceof List) {
                document.put(descriptor.getKey(), buildDBList(node));
            } else {
                document.put(descriptor.getKey(), value);
            }
        }

        return document;
    }

    @SuppressWarnings("unchecked") // Since we're adding heterogeneous items in our list
    private static List buildDBList(NoSqlTreeNode parentNode) {
        final List list = new ArrayList();
        Enumeration children = parentNode.children();
        while (children.hasMoreElements()) {
            NoSqlTreeNode node = (NoSqlTreeNode) children.nextElement();
            MongoValueDescriptor descriptor = (MongoValueDescriptor) node.getDescriptor();
            Object value = descriptor.getValue();

            if (value instanceof Document) {
                list.add(buildDBDocument(node));
            } else if (value instanceof List) {
                list.add(buildDBList(node));
            } else {
                list.add(value);
            }
        }
        return list;
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
