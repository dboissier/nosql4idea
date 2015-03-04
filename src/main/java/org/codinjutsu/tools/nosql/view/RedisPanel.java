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

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.database.mongo.model.MongoCollection;
import org.codinjutsu.tools.nosql.database.mongo.model.MongoResult;
import org.codinjutsu.tools.nosql.database.redis.RedisManager;
import org.codinjutsu.tools.nosql.database.redis.RedisQuery;
import org.codinjutsu.tools.nosql.database.redis.model.RedisDatabase;
import org.codinjutsu.tools.nosql.database.redis.model.RedisResult;
import org.codinjutsu.tools.nosql.view.model.RedisTreeModel;

import javax.swing.*;
import java.awt.*;

public class RedisPanel extends NoSqlResultView {

    private JPanel toolBarPanel;
    private JPanel resultPanel;
    private JPanel errorPanel;

    private JsonTreeTableView resultTableView;

    private final Project project;
    private final RedisManager redisManager;
    private final ServerConfiguration configuration;
    private final RedisDatabase database;

    public RedisPanel(Project project, RedisManager redisManager, ServerConfiguration configuration, RedisDatabase database) {
        this.project = project;
        this.redisManager = redisManager;
        this.configuration = configuration;
        this.database = database;

        RedisResult redisResult = redisManager.loadRecords(configuration, new RedisQuery());
        updateResultTableTree(redisResult);
    }

    public void updateResultTableTree(RedisResult redisResult) {
        resultTableView = new JsonTreeTableView(RedisTreeModel.buildTree(redisResult), JsonTreeTableView.COLUMNS_FOR_READING);
        resultTableView.setName("resultTreeTable");

        resultPanel.invalidate();
        resultPanel.removeAll();
        resultPanel.add(new JBScrollPane(resultTableView));
        resultPanel.validate();

        setLayout(new BorderLayout());
        add(resultPanel, BorderLayout.CENTER);
    }

    @Override
    public void showResults() {

    }

    @Override
    public JPanel getResultPanel() {
        return resultPanel;
    }

    @Override
    public void dispose() {

    }
}
