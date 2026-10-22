package com.art21studio.maybenormalplugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/*
Класс Main описывает логику взаимодействия всех остальных классов плагина
Конструктор класса состоит из одного параметра:
- project (Project) объект активного проекта в IDE
 */
@Service(Service.Level.PROJECT)
class Main(private val project: Project): Disposable {
    init {Logger.getInstance(Main::class.java).info("main loaded")}
    private val filer: FileManager by lazy { FileManager(project) }
    private val highlighter: HighLighterManager by lazy { HighLighterManager(project) }
    /*
    Метод init запускает чтение записей о цветных регионах, после чего запускает окрашивание документов в цветные регионы
    Метод ничего не принимает в качестве аргументов
    Метод ничего не возвращает
     */
    fun init(){
        val projectDirectory: VirtualFile = project.baseDir ?: return
        WriteAction.run<Throwable> {if(projectDirectory.findChild(".colorRegion") == null) projectDirectory.createChildDirectory(this,".colorRegion")}
        val base: MutableList<MutableList<ColorRegion>> = ReadAction.compute<MutableList<MutableList<ColorRegion>>, Throwable> {filer.init()}
        ApplicationManager.getApplication().invokeLater {highlighter.init(base, filer.files)}
    }
    /*
    Метод dispose реализует логику уничтожения объекта класса
    Метод ничего не принимает в качестве аргументов
    Метод ничего не возвращает
     */
    override fun dispose() {
        filer.dispose()
        highlighter.dispose()
    }
}