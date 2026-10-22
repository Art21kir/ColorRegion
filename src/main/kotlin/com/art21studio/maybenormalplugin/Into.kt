package com.art21studio.maybenormalplugin

import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Into: ProjectActivity {
    override suspend fun execute(project: Project) {
        withContext(Dispatchers.EDT){project.getService(Main::class.java).init()}
        Logger.getInstance(Into::class.java).info("ColorRegion")
    }
}