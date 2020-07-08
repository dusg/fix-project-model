package org.dusg.fixProjectModule.services

import com.intellij.openapi.project.Project
import org.dusg.fixProjectModule.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
