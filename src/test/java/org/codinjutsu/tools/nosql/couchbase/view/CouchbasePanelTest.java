package org.codinjutsu.tools.nosql.couchbase.view;

import com.couchbase.client.java.document.json.JsonObject;
import com.intellij.openapi.command.impl.DummyProject;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.view.TableCellReader;
import org.codinjutsu.tools.nosql.couchbase.logic.CouchbaseClient;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseDatabase;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseQuery;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseResult;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.Containers;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class CouchbasePanelTest {

    private FrameFixture frameFixture;

    private CouchbaseClient couchbaseClientMock = Mockito.mock(CouchbaseClient.class);
    private CouchbasePanel couchbasePanelWrapper;

    @Before
    public void setUp() throws Exception {
        when(couchbaseClientMock.loadRecords(any(ServerConfiguration.class), any(CouchbaseDatabase.class), any(CouchbaseQuery.class))).thenReturn(new CouchbaseResult("dummy"));


        couchbasePanelWrapper = GuiActionRunner.execute(new GuiQuery<CouchbasePanel>() {
            protected CouchbasePanel executeInEDT() {
                return new CouchbasePanel(DummyProject.getInstance(),
                        couchbaseClientMock,
                        new ServerConfiguration(),
                        new CouchbaseDatabase("default"));
            }
        });

        frameFixture = Containers.showInFrame(couchbasePanelWrapper);
    }

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Test
    public void displayJsonObjects() throws Exception {
        couchbasePanelWrapper.updateResultTableTree(createResults());

        frameFixture.table("resultTreeTable").cellReader(new TableCellReader())
                .requireColumnCount(2)
                .requireContents(new String[][]{
                        {"amount", "123456764"},
                        {"mad", "true"},
                        {"address", "{\"City\":\"Paris\",\"ZIP Code\":75016,\"Street\":\"Av. Champs Elysées\"}"},
                        {"Street", "\"Av. Champs Elysées\""},
                        {"ZIP Code", "75016"},
                        {"City", "\"Paris\""},
                        {"interests", "[\"programming\",\"XP\",\"TDD\"]"},
                        {"[0]", "\"programming\""},
                        {"[1]", "\"XP\""},
                        {"[2]", "\"TDD\""},
                        {"age", "25"},
                        {"score", "12345.12121"},
                        {"firstname", "\"Jojo\""},
                });
    }

    private CouchbaseResult createResults() {
        CouchbaseResult result = new CouchbaseResult("test");
        result.add(JsonObject.create().put("firstname", "Jojo")
                .put("age", 25)
                .put("mad", true)
                .put("interests", Arrays.asList("programming", "XP", "TDD"))
                .put("amount", 123456764L)
                .put("score", 12345.12121d)
                .put("address", JsonObject.create()
                        .put("Street", "Av. Champs Elysées")
                        .put("City", "Paris")
                        .put("ZIP Code", 75016)));
        return result;
    }
}