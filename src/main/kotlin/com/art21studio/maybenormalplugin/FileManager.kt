package com.art21studio.maybenormalplugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.intellij.openapi.vfs.readText
import org.yaml.snakeyaml.Yaml
import java.awt.Color

/*
Класс FileManager описывает логику работы с YAML файлами в директории /.ColorRegion
Конструктор класса состоит из одного параметра:
- project (Project) объект активного проекта в IDE
 */
@Service(Service.Level.PROJECT)
class FileManager (project: Project): Disposable {
    private val yamlMap: MutableMap<String, String> = mutableMapOf()
    val files: MutableList<String> = mutableListOf()
    private val colorRegionDirectory: VirtualFile = project.baseDir.findDirectory(".colorRegion")!!
    /*
    Метод init реализует чтение записей о цветных регионах для каждого файла, записи о котором нашлись в /.ColorRegion
    Метод ничего не принимает в качестве аргументов
    Метод возвращает MutableList<MutableList<ColorRegion>> - списки цветных регионов для каждого найденного файла
     */
    fun init() : MutableList<MutableList<ColorRegion>> {
        val regions: MutableList<MutableList<ColorRegion>> = mutableListOf()
        val mapFile: VirtualFile = colorRegionDirectory.findFileByRelativePath("map.yml") ?: return regions
        val mapData: Map<Int, Any> = Yaml().load(mapFile.readText())
        for ((key, value) in mapData){
            yamlMap[value.toString()] = key.toString()
            val fileRegions: MutableList<ColorRegion> = mutableListOf()
            val file: VirtualFile? = colorRegionDirectory.findFileByRelativePath("$key.yml")
            if (file != null){
                val fileData: Map<Int, Any> = Yaml().load(file.readText())
                for ((fileKey, fileValue) in fileData){
                    if (fileValue is Map<*, *>)
                        try {
                            Logger.getInstance("ColorRegion").info(fileValue.toString())
                            fileRegions.add(ColorRegion(fileValue["start"] as Int, fileValue["finish"] as Int, Color.decode(fileValue["color"] as String)))
                        }
                        catch (e: Exception){
                            Logger.getInstance("ColorRegion").info(e.toString())
                        }
                }
            }
            regions.add(fileRegions)
            files.add(value.toString())
        }
        Logger.getInstance("ColorRegion").info(regions.toString())
        return regions
    }
/*
Метод dispose реализует логику уничтожения объекта класса
Метод ничего не принимает в качестве аргументов
Метод ничего не возвращает
 */
    override fun dispose() {
        yamlMap.clear()
    }
}