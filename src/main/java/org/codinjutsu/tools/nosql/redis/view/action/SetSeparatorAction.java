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

package org.codinjutsu.tools.nosql.redis.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ArrayUtil;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.redis.view.RedisPanel;

import java.util.LinkedHashSet;
import java.util.Set;

public class SetSeparatorAction extends AnAction {

    private final RedisPanel redisPanel;

    private final Set<String> myPredefinedSeparators = new LinkedHashSet<>();


    public SetSeparatorAction(RedisPanel redisPanel) {
        super(AllIcons.General.Ellipsis);
        this.redisPanel = redisPanel;
        myPredefinedSeparators.add(".");
        myPredefinedSeparators.add(":");
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        String[] strings = ArrayUtil.toStringArray(myPredefinedSeparators);
        String current = redisPanel.getGroupSeparator();
        String separator = Messages.showEditableChooseDialog("Redis Keys Separator",
                "Select Separator",
                Messages.getQuestionIcon(),
                strings, current, null);
        if (separator == null) {
            return;
        }

        if (StringUtils.equals(redisPanel.getGroupSeparator(), separator)) {
            return;
        }

        redisPanel.setGroupSeparator(separator);

        myPredefinedSeparators.add(separator);
        update(event);
    }

    @Override
    public void update(AnActionEvent event) {
        String currentSeparator = redisPanel.getGroupSeparator();
        String textToDisplay = String.format("Group by '%s'", (currentSeparator == null ? "Nothing" : currentSeparator));
        event.getPresentation().setText(textToDisplay);
        event.getPresentation().setDescription(textToDisplay);
        event.getPresentation().setEnabled(redisPanel.isGroupDataEnabled());
    }
}
