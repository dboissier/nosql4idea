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

package org.codinjutsu.tools.nosql.mongo.view;

import com.intellij.openapi.command.impl.DummyProject;
import com.intellij.util.ui.tree.TreeUtil;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.codinjutsu.tools.nosql.commons.view.TableCellReader;
import org.codinjutsu.tools.nosql.mongo.MongoUtils;
import org.codinjutsu.tools.nosql.mongo.model.MongoResult;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.Containers;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MongoResultPanelTest {

    private MongoResultPanel mongoResultPanel;

    private FrameFixture frameFixture;

    @Mock
    private MongoPanel.MongoDocumentOperations mongoDocumentOperations;

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(MongoResultPanelTest.class);

        mongoResultPanel = GuiActionRunner.execute(new GuiQuery<MongoResultPanel>() {
            protected MongoResultPanel executeInEDT() {
                return new MongoResultPanel(DummyProject.getInstance(), mongoDocumentOperations) {
                    @Override
                    void buildPopupMenu() {
                    }
                };
            }
        });

        frameFixture = Containers.showInFrame(mongoResultPanel);
    }

    @Test
    public void displayTreeWithASimpleArray() throws Exception {
        mongoResultPanel.updateResultTableTree(createCollectionResults("simpleArray.json", "mycollec"));

        frameFixture.table("resultTreeTable").cellReader(new TableCellReader())
                .requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "\"toto\""},
                        {"[1]", "true"},
                        {"[2]", "10"},
                        {"[3]", "null"},
                });
    }

    @Test
    public void testDisplayTreeWithASimpleDocument() throws Exception {
        mongoResultPanel.updateResultTableTree(createCollectionResults("simpleDocument.json", "mycollec"));

        frameFixture.table("resultTreeTable").cellReader(new TableCellReader())
                .requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "Document{{_id=50b8d63414f85401b9268b99, label=toto, visible=false, image=null}}"},
                        {"_id", "\"50b8d63414f85401b9268b99\""},
                        {"label", "\"toto\""},
                        {"visible", "false"},
                        {"image", "null"}
                });
    }


    @Test
    public void testDisplayTreeWithAStructuredDocument() throws Exception {
        mongoResultPanel.updateResultTableTree(createCollectionResults("structuredDocument.json", "mycollec"));
        TreeUtil.expandAll(mongoResultPanel.resultTableView.getTree());
        frameFixture.table("resultTreeTable").cellReader(new TableCellReader())
                .requireColumnCount(2)
                .requireContents(new String[][]{
                        {"[0]", "Document{{id=0, label=toto, visible=false, doc=Document{{title=hello, nbPages=10, keyWord=[toto, true, 10]}}}}"},
                        {"id", "0"},
                        {"label", "\"toto\""},
                        {"visible", "false"},
                        {"doc", "Document{{title=hello, nbPages=10, keyWord=[toto, true, 10]}}"},
                        {"title", "\"hello\""},
                        {"nbPages", "10"},
                        {"keyWord", "[toto, true, 10]"},
                        {"[0]", "\"toto\""},
                        {"[1]", "true"},
                        {"[2]", "10"},
                });
    }


    @Test
    public void testDisplayTreeWithAnArrayOfStructuredDocument() throws Exception {
        mongoResultPanel.updateResultTableTree(createCollectionResults("arrayOfDocuments.json", "mycollec"));

        TreeUtil.expandAll(mongoResultPanel.resultTableView.getTree());
        frameFixture.table("resultTreeTable").cellReader(new TableCellReader())
                .requireContents(new String[][]{

                        {"[0]", "Document{{id=0, label=toto, visible=false, doc=Document{{title=hello, nbPages=10, keyWord=[toto, true, 10]}}}}"},
                        {"id", "0"},
                        {"label", "\"toto\""},
                        {"visible", "false"},
                        {"doc", "Document{{title=hello, nbPages=10, keyWord=[toto, true, 10]}}"},
                        {"title", "\"hello\""},
                        {"nbPages", "10"},
                        {"keyWord", "[toto, true, 10]"},
                        {"[0]", "\"toto\""},
                        {"[1]", "true"},
                        {"[2]", "10"},
                        {"[1]", "Document{{id=1, label=tata, visible=true, doc=Document{{title=ola, nbPages=1, keyWord=[tutu, false, 10]}}}}"},
                        {"id", "1"},
                        {"label", "\"tata\""},
                        {"visible", "true"},
                        {"doc", "Document{{title=ola, nbPages=1, keyWord=[tutu, false, 10]}}"},
                        {"title", "\"ola\""},
                        {"nbPages", "1"},
                        {"keyWord", "[tutu, false, 10]"},
                        {"[0]", "\"tutu\""},
                        {"[1]", "false"},
                        {"[2]", "10"},
                });
    }

    @Test
    public void testCopyMongoObjectNodeValue() throws Exception {
        mongoResultPanel.updateResultTableTree(createCollectionResults("structuredDocument.json", "mycollec"));
        TreeUtil.expandAll(mongoResultPanel.resultTableView.getTree());

        mongoResultPanel.resultTableView.setRowSelectionInterval(0, 0);
        assertEquals("Document{{id=0, label=toto, visible=false, doc=Document{{title=hello, nbPages=10, keyWord=[toto, true, 10]}}}}", mongoResultPanel.getSelectedNodeStringifiedValue());

        mongoResultPanel.resultTableView.setRowSelectionInterval(2, 2);
        assertEquals("\"label\" : \"toto\"", mongoResultPanel.getSelectedNodeStringifiedValue());

        mongoResultPanel.resultTableView.setRowSelectionInterval(4, 4);
        assertEquals("\"doc\" : Document{{title=hello, nbPages=10, keyWord=[toto, true, 10]}}", mongoResultPanel.getSelectedNodeStringifiedValue());
    }

    @Test
    public void copyMongoResults() throws Exception {
        mongoResultPanel.updateResultTableTree(createCollectionResults("arrayOfDocuments.json", "mycollec"));

        TreeUtil.expandAll(mongoResultPanel.resultTableView.getTree());

        frameFixture.table("resultTreeTable").cellReader(new TableCellReader())
                .requireContents(new String[][]{
                        {"[0]", "Document{{id=0, label=toto, visible=false, doc=Document{{title=hello, nbPages=10, keyWord=[toto, true, 10]}}}}"},
                        {"id", "0"},
                        {"label", "\"toto\""},
                        {"visible", "false"},
                        {"doc", "Document{{title=hello, nbPages=10, keyWord=[toto, true, 10]}}"},
                        {"title", "\"hello\""},
                        {"nbPages", "10"},
                        {"keyWord", "[toto, true, 10]"},
                        {"[0]", "\"toto\""},
                        {"[1]", "true"},
                        {"[2]", "10"},
                        {"[1]", "Document{{id=1, label=tata, visible=true, doc=Document{{title=ola, nbPages=1, keyWord=[tutu, false, 10]}}}}"},
                        {"id", "1"},
                        {"label", "\"tata\""},
                        {"visible", "true"},
                        {"doc", "Document{{title=ola, nbPages=1, keyWord=[tutu, false, 10]}}"},
                        {"title", "\"ola\""},
                        {"nbPages", "1"},
                        {"keyWord", "[tutu, false, 10]"},
                        {"[0]", "\"tutu\""},
                        {"[1]", "false"},
                        {"[2]", "10"},
                });

        assertEquals("[ " +
                        "Document{{id=0, label=toto, visible=false, doc=Document{{title=hello, nbPages=10, keyWord=[toto, true, 10]}}}} , " +
                        "Document{{id=1, label=tata, visible=true, doc=Document{{title=ola, nbPages=1, keyWord=[tutu, false, 10]}}}}" +
                        " ]",
                mongoResultPanel.getSelectedNodeStringifiedValue());
    }

    private MongoResult createCollectionResults(String data, String collectionName) throws IOException {
        Object jsonObject = MongoUtils.parseJSON(IOUtils.toString(getClass().getResourceAsStream(data)));

        MongoResult mongoResult = new MongoResult(collectionName);

        if (jsonObject instanceof List) {
            for (Object document : ((List) jsonObject)) {
                mongoResult.add((Document) document);
            }
        } else {
            mongoResult.add((Document) jsonObject);
        }

        return mongoResult;
    }

}
