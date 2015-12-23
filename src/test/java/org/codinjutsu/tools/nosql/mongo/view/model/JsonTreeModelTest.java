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

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.nosql.commons.view.NoSqlTreeNode;
import org.codinjutsu.tools.nosql.mongo.MongoUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class JsonTreeModelTest {

    @Test
    public void buildDBObjectFromSimpleTree() throws Exception {
        Document jsonObject = Document.parse(IOUtils.toString(getClass().getResourceAsStream("simpleDocument.json")));

//        Hack to convert _id from string to ObjectId
        jsonObject.put("_id", new ObjectId(String.valueOf(jsonObject.get("_id"))));

        NoSqlTreeNode treeNode = (NoSqlTreeNode) JsonTreeModel.buildJsonTree(jsonObject);
        NoSqlTreeNode labelNode = (NoSqlTreeNode) treeNode.getChildAt(1);
        labelNode.getDescriptor().setValue("tata");


        Document dbObject = JsonTreeModel.buildDBDocument(treeNode);

        assertEquals("Document{{_id=50b8d63414f85401b9268b99, label=tata, visible=false, image=null}}",
                dbObject.toString());

        assertTrue(dbObject.get("_id") instanceof ObjectId);
    }

    @Test
    public void buildDBObjectFromTreeWithSubNodes() throws Exception {
        Document jsonObject = Document.parse(IOUtils.toString(getClass().getResourceAsStream("simpleDocumentWithInnerNodes.json")));

//        Hack to convert _id fron string to ObjectId
        jsonObject.put("_id", new ObjectId(String.valueOf(jsonObject.get("_id"))));

        NoSqlTreeNode treeNode = (NoSqlTreeNode) JsonTreeModel.buildJsonTree(jsonObject);

//      Simulate updating from the treeNode
        NoSqlTreeNode innerDocNode = (NoSqlTreeNode) treeNode.getChildAt(4);
        NoSqlTreeNode soldOutNode = (NoSqlTreeNode) innerDocNode.getChildAt(2);
        soldOutNode.getDescriptor().setValue("false");

        Document dbObject = JsonTreeModel.buildDBDocument(treeNode);

        assertEquals("Document{{_id=50b8d63414f85401b9268b99, label=toto, visible=false, image=null, innerdoc=Document{{title=What?, numberOfPages=52, soldOut=false}}}}",
                dbObject.toString());

        assertTrue(dbObject.get("_id") instanceof ObjectId);
    }

    @Test
    public void buildDBObjectFromTreeWithSubList() throws Exception {
        Document jsonObject = Document.parse(IOUtils.toString(getClass().getResourceAsStream("simpleDocumentWithSubList.json")));

//        Hack to convert _id fron string to ObjectId
        jsonObject.put("_id", new ObjectId(String.valueOf(jsonObject.get("_id"))));

        NoSqlTreeNode treeNode = (NoSqlTreeNode) JsonTreeModel.buildJsonTree(jsonObject);
        NoSqlTreeNode tagsNode = (NoSqlTreeNode) treeNode.getChildAt(2);
        NoSqlTreeNode agileTagNode = (NoSqlTreeNode) tagsNode.getChildAt(2);
        agileTagNode.getDescriptor().setValue("a gilles");

        Document dbObject = JsonTreeModel.buildDBDocument(treeNode);

        assertEquals("Document{{_id=50b8d63414f85401b9268b99, title=XP by example, tags=[pair programming, tdd, a gilles], innerList=[[1, 2, 3, 4], [false, true], [Document{{tagName=pouet}}, Document{{tagName=paf}}]]}}",
                dbObject.toString());

        assertTrue(dbObject.get("_id") instanceof ObjectId);
    }

    @Test
    public void getObjectIdFromANode() throws Exception {
        Document jsonObject = Document.parse(IOUtils.toString(getClass().getResourceAsStream("simpleDocumentWithInnerNodes.json")));
        jsonObject.put("_id", new ObjectId(String.valueOf(jsonObject.get("_id"))));

        NoSqlTreeNode treeNode = (NoSqlTreeNode) JsonTreeModel.buildJsonTree(jsonObject);
        NoSqlTreeNode objectIdNode = (NoSqlTreeNode) treeNode.getChildAt(0);
        assertEquals("_id", objectIdNode.getDescriptor().getFormattedKey());

        assertNull(JsonTreeModel.findObjectIdNode(treeNode));
        assertEquals(objectIdNode, JsonTreeModel.findObjectIdNode((NoSqlTreeNode) treeNode.getChildAt(0)));

    }

    @Test
    public void findDocumentFromANode() throws Exception {
        List dbList = (List) MongoUtils.parseJSON(IOUtils.toString(getClass().getResourceAsStream("arrayOfDocuments.json")));

        Document first = (Document) dbList.get(0);
        first.put("_id", new ObjectId(String.valueOf(first.get("_id"))));

        Document second = (Document) dbList.get(1);
        second.put("_id", new ObjectId(String.valueOf(second.get("_id"))));

        NoSqlTreeNode treeNode = (NoSqlTreeNode) JsonTreeModel.buildJsonTree(dbList);

        assertEquals(first, JsonTreeModel.findDocument((NoSqlTreeNode) treeNode.getChildAt(0)));
    }
}
