package com.art21studio.maybenormalplugin

import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
Класс Intro подписываетсся на событие открытия проекта в IDE и является начальной точкой работы плагина
Конструктор класса не состоит из аргументов
 */
class Into: ProjectActivity {
    /*
    Переопределённый метод execute вызывает создание основного объекта плагина Main
    Метод принимает 1 аргумент:
    - project (Project) объект проекта, открытого в IDE
    Метод ничего не возвращает
     */
    override suspend fun execute(project: Project) {
        withContext(Dispatchers.EDT){project.getService(Main::class.java).init()}
        Logger.getInstance(Into::class.java).info("ColorRegion")
    }
}