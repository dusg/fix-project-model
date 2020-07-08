package org.dusg.fixProjectModule.actions;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


class FixModuleAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        @Nullable Module module =
                moduleManager.findModuleByName("com.enflame.profiler");
        if (module == null) {
            return ;
        }
        Module depCore = moduleManager.findModuleByName("com.enflame.core");

        if (depCore == null) {
            return;
        }
        Module depDe = moduleManager.findModuleByName("de.jaret.util");
        if (depDe == null) {
            return;
        }
//        ModuleRootManager rootModule = ModuleRootManager.getInstance(module);
//        rootModule.getModifiableModel().addModuleOrderEntry(depCore);

        ModuleRootModificationUtil.updateModel(module, modifiableRootModel -> {
            doAddModule(modifiableRootModel, depCore);
            doAddModule(modifiableRootModel, depDe);
        });

        ModuleRootModificationUtil.updateModel(depCore, modifiableRootModel -> {
            doAddModule(modifiableRootModel, depDe);
        });

        new MyNotifier().notify(project, "Success fix project.");
//        ModuleRootManagerEx.getInstanceEx(module).orderEntries
    }

    private void doAddModule(ModifiableRootModel modifiableRootModel, Module dep) {
        ModuleRootManager manager = ModuleRootManager.getInstance(modifiableRootModel.getModule());
        if (manager.isDependsOn(dep)) {
            return;
        }
        ModuleOrderEntry moduleOrderEntry = modifiableRootModel.addModuleOrderEntry(dep);
        moduleOrderEntry.setScope(DependencyScope.COMPILE);
        moduleOrderEntry.setExported(false);
        List<@NotNull OrderEntry> entries = new ArrayList<>(Arrays.asList(modifiableRootModel.getOrderEntries()));
        OrderEntry lastEntry = entries.get(entries.size() - 1);
        entries.remove(lastEntry);
        entries.add(2, lastEntry);
        modifiableRootModel.rearrangeOrderEntries(entries.toArray(new OrderEntry[0]));
    }
}

class MyNotifier {
    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Fix Project Module",
            NotificationDisplayType.BALLOON, true);

    public Notification notify(String content) {
        return notify(null, content);
    }

    public Notification notify(Project project, String content) {
        final Notification notification = NOTIFICATION_GROUP.createNotification(content, NotificationType.INFORMATION);
        notification.notify(project);
        return notification;
    }
}