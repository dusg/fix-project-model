package org.dusg.fixProjectModule.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ex.ClipboardUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.ex.FileChooserDialogImpl;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ShareClipboardAction extends AnAction {

    static Task.Backgroundable task;
    @Override
    public void update(@NotNull AnActionEvent e) {
        if (task == null) {
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(task.isCancellable());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FileChooserDescriptor singleFileDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        FileChooserDialogImpl fileChooserDialog = new FileChooserDialogImpl(singleFileDescriptor, e.getProject());
        @NotNull VirtualFile[] files = fileChooserDialog.choose(null);
        if (files.length == 0) {
            return;
        }
        VirtualFile file = files[0];
        task = new ClipboardTask(e.getProject(), file);
        ProgressManager.getInstance().run(task);
    }

    class ClipboardTask extends Task.Backgroundable {

        private final VirtualFile file;
        private long lastStamp = 0;
        private String lastContend;

        public ClipboardTask(@Nullable Project project, VirtualFile filePath) {
            super(project, "Clipboard Server listening...", true);
            file = filePath;
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            listeningClipboard(indicator);
        }
        private void listeningClipboard(@NotNull ProgressIndicator indicator) {
            for (; ; ) {
                if (indicator.isCanceled()) {
                    System.out.println("Stop clipboard server");
                    return;
                }
                try {
                    updateClipboardFromFile();
                    updateFileContentFromClipboard();
<<<<<<< HEAD
                    Thread.sleep(100);
=======
                    Thread.sleep(500);
>>>>>>> 增加粘贴板共享功能
                } catch (IOException | UnsupportedFlavorException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        private void updateFileContentFromClipboard() throws IOException, UnsupportedFlavorException {
            String clipboardString = ClipboardUtils.getClipboardString();
            if (clipboardString.equals(lastContend)) {
                return;
            }
            Files.writeString(Paths.get(file.getPath()), clipboardString, StandardCharsets.UTF_8);
            lastContend = clipboardString;
            System.out.println("update shared file: " + lastContend);
        }

        private void updateClipboardFromFile() throws IOException {
            long timeStamp = file.getTimeStamp();
            if (timeStamp == lastStamp) {
                return;
            }

            String contend = Files.readString(Paths.get(file.getPath()), file.getCharset());
            if (contend.equals(lastContend)) {
                return;
            }
            ClipboardUtils.setClipboardString(lastContend);
            System.out.println("update clipboard: " + lastContend);
        }
    }
}
