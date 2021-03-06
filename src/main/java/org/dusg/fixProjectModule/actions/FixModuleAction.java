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
        @Nullable Module profilerModule =
                moduleManager.findModuleByName("com.enflame.profiler");
        if (profilerModule == null) {
            return ;
        }
        Module coreModule = moduleManager.findModuleByName("com.enflame.core");

        if (coreModule == null) {
            return;
        }
        Module jaretModule = moduleManager.findModuleByName("de.jaret.util");
        if (jaretModule == null) {
            return;
        }

        ModuleRootModificationUtil.updateModel(profilerModule, modifiableRootModel -> {
            doAddModule(modifiableRootModel, coreModule);
            doAddModule(modifiableRootModel, jaretModule);
        });

        ModuleRootModificationUtil.updateModel(coreModule, modifiableRootModel -> {
            doAddModule(modifiableRootModel, jaretModule);
        });
        Module graphModule = moduleManager.findModuleByName("com.enflame.model.graph");
        if (graphModule == null) {
            return;
        }
        ModuleRootModificationUtil.updateModel(graphModule, modifiableRootModel -> {
            doAddModule(modifiableRootModel, coreModule);
        });

        Module profilerTestsModule = moduleManager.findModuleByName("com.enflame.profiler.tests");
        if (profilerTestsModule != null) {
            ModuleRootModificationUtil.updateModel(profilerTestsModule, modifiableRootModel -> {
                doAddModule(modifiableRootModel, coreModule);
                doAddModule(modifiableRootModel, profilerModule);
            });

        }
        Module graphTests = moduleManager.findModuleByName("com.enflame.model.graph.tests");
        if (graphTests != null) {
            ModuleRootModificationUtil.updateModel(graphTests, modifiableRootModel -> {
                doAddModule(modifiableRootModel, graphModule);
                doAddModule(modifiableRootModel, coreModule);
            });
        }
        Module graphUITests = moduleManager.findModuleByName("com.enflame.model.graph.uitests");
        if (graphUITests != null) {
            ModuleRootModificationUtil.updateModel(graphUITests, modifiableRootModel -> {
                doAddModule(modifiableRootModel, graphModule);
                doAddModule(modifiableRootModel, coreModule);
            });
        }
        new MyNotifier().notify(project, "Success fix project.");
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

