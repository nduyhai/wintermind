package com.github.nduyhai.wintermind.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class WinterMindProjectService(project: Project) {


    fun prompt(txt: String): String {
        return "TODO"
    }
}
