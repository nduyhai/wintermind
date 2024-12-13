package com.github.nduyhai.wintermind.services

import com.github.nduyhai.wintermind.WinterMindBundle
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class WinterMindProjectService(project: Project) {

    init {
        thisLogger().info(WinterMindBundle.message("projectService", project.name))
    }

    fun prompt(txt: String): String {
        return "pong pong"
    }
}
