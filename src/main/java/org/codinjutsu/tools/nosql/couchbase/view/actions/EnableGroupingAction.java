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

package org.codinjutsu.tools.nosql.couchbase.view.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.codinjutsu.tools.nosql.redis.view.RedisPanel;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class EnableGroupingAction extends ToggleAction {
    private final RedisPanel redisPanel;

    public EnableGroupingAction(RedisPanel redisPanel) {
        super("Group data", "Build a hierarchy based on separator", AllIcons.Actions.GroupByPrefix);
        this.redisPanel = redisPanel;

        registerCustomShortcutSet(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK, redisPanel);
    }


    @Override
    public boolean isSelected(AnActionEvent e) {
        return redisPanel.isGroupDataEnabled();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        redisPanel.toggleGroupData(state);
        redisPanel.renderRecords();
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(redisPanel.getRecords() != null);
    }
}
