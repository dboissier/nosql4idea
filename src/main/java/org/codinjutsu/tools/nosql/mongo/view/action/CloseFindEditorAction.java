package org.codinjutsu.tools.nosql.mongo.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.CloseTabToolbarAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.nosql.mongo.view.MongoPanel;

import java.awt.event.KeyEvent;

public class CloseFindEditorAction extends CloseTabToolbarAction {
    private final MongoPanel mongoPanel;

    public CloseFindEditorAction(MongoPanel mongoPanel) {
        getTemplatePresentation().setIcon(AllIcons.Actions.Close);
        registerCustomShortcutSet(KeyEvent.VK_ESCAPE, 0, mongoPanel);
        this.mongoPanel = mongoPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        mongoPanel.closeFindEditor();
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(false);
    }
}