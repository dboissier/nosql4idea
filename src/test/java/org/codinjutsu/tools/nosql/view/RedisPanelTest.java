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

package org.codinjutsu.tools.nosql.view;

import com.intellij.openapi.command.impl.DummyProject;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.database.redis.RedisManager;
import org.codinjutsu.tools.nosql.database.redis.model.RedisDatabase;
import org.codinjutsu.tools.nosql.database.redis.model.RedisResult;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.Containers;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Tuple;

import java.util.*;

public class RedisPanelTest {

    private RedisPanel redisPanel;

    private FrameFixture frameFixture;

    private Project dummyProject = DummyProject.getInstance();

    @After
    public void tearDown() {
        frameFixture.cleanUp();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(MongoResultPanelTest.class);

        redisPanel = GuiActionRunner.execute(new GuiQuery<RedisPanel>() {
            protected RedisPanel executeInEDT() {
                return new RedisPanel(dummyProject, Mockito.mock(RedisManager.class), new ServerConfiguration(), new RedisDatabase("0"));
            }
        });

        frameFixture = Containers.showInFrame(redisPanel);
    }

    @Test
    public void displayTreeWithEachSupportedKeyType() throws Exception {
        redisPanel.updateResultTableTree(createRedisResults());

        frameFixture.table("resultTreeTable").cellReader(new TableCellReader())
                .requireColumnCount(2)
                .requireContents(new String[][]{
                        {"\"foo\"", "bar"},
                        {"\"fun\"", "[drink, some, beer]"},
                        {"[0]", "drink"},
                        {"[1]", "some"},
                        {"[2]", "beer"},
                        {"\"countries\"", "{France, Canada, Japan}"},
                        {"-", "France"},
                        {"-", "Canada"},
                        {"-", "Japan"},
                        {"\"aliases\"", "{david=dada, mickael=mike, bruno=nono}"},
                        {"\"david\"", "dada"},
                        {"\"mickael\"", "mike"},
                        {"\"bruno\"", "nono"},
                        {"\"critics\"", "{(unreal, 8.0), (quake, 9.0), (half-life, 10.0)}"},
                        {"-", "(unreal, 8.0)"},
                        {"-", "(quake, 9.0)"},
                        {"-", "(half-life, 10.0)"},
                });
    }

    private RedisResult createRedisResults() {
        RedisResult redisResult = new RedisResult();
        redisResult.setSeparator(":");
        redisResult.addString("foo", "bar");
        redisResult.addList("fun", Arrays.asList("drink", "some", "beer"));
        Set<String> countries = new HashSet<String>();
        countries.add("France");
        countries.add("Japan");
        countries.add("Canada");
        redisResult.addSet("countries", countries);

        Map<String, String> aliasByPeopleName = new HashMap<String, String>();
        aliasByPeopleName.put("david", "dada");
        aliasByPeopleName.put("mickael", "mike");
        aliasByPeopleName.put("bruno", "nono");
        redisResult.addHash("aliases", aliasByPeopleName);

        SortedSet<Tuple> scoreByGameTitle = new TreeSet<Tuple>();
        scoreByGameTitle.add(new Tuple("quake", 9d));
        scoreByGameTitle.add(new Tuple("half-life", 10d));
        scoreByGameTitle.add(new Tuple("unreal", 8d));

        redisResult.addSortedSet("critics", scoreByGameTitle);
        return redisResult;
    }

}