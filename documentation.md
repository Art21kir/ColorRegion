# Документация к плагину `ColorRegion`
## Общее описание решения
Плагин `ColorRegion` предназначен для цветовой разметки строк тех файлов проекта, что представимы как `PlainText`.
Плагин совместим с IDE `Clion` версии `2023.2.2`
## Здесь и далее
`Цветной регион` - область кода, которая раскрашена в цвет
## Описание всех классов
### Into.kt
Данный `.kt` файл содержит в себе реализацию класса `Into : ProjectActivity`

Конструктор класса не содержит аргументов

Класс наследуется от `ProjectActivity`
#### execute
Метод - входная точка работы всего плагина. Создаёт главный рабочий класс плагина
##### Аргументы
```
project <Project> - объект активного проекта в IDE
```
##### Возвращаемые значения
Метод ничего не возвращает
##### Реализация
```kotlin
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
```
### Main.kt
Данный `.kt` файл содержит в себе реализацию класса `Main : Disposable`

Конструктор класса содержит следующие аргументы:
```
project <Project> - объект активного проекта в IDE
```

Класс наследуется от `Disposablse`
#### init
Метод инициализации основной логики плагина
##### Аргументы
Метод ничего не принимает в качестве аргументов
##### Возвращаемые значения
Метод ничего не возвращает
##### Реализация
```kotlin
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
```
#### dispose
Метод уничтожения класса, который наследуется от `Disposable`
##### Аргументы
Метод ничего не принимает в качестве аргументов
##### Возвращаемые значения
Метод ничего не возвращает
##### Реализация
```kotlin
     /*
    Метод dispose реализует логику уничтожения объекта класса
    Метод ничего не принимает в качестве аргументов
    Метод ничего не возвращает
     */
override fun dispose() {
    filer.dispose()
    highlighter.dispose()
}
```
### FileManager.kt
Данный `.kt` файл содержит в себе реализацию класса `FileManager : Disposable`

Конструктор класса содержит следующие аргументы:
```
project <Project> - объект активного проекта в IDE
```

Класс наследуется от `Disposablse`
#### init
Метод инициализации чтения YAML файлов в директории `/.ColorRegion` - записей о цветных регионах каждого файла
##### Аргументы
Метод ничего не принимает в качестве аргументов
##### Возвращаемые значения
Метод вовзвращает 1 значение:
```
regions <MutableList<MutableList<ColorRegion>>> - список всех считанных из файлов списков регионов для файлов. Индекс такого списка связан с индексом файла
```
##### Реализация
```kotlin
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
```
#### dispose
Метод уничтожения класса, который наследуется от `Disposable`
##### Аргументы
Метод ничего не принимает в качестве аргументов
##### Возвращаемые значения
Метод ничего не возвращает
##### Реализация
```kotlin
    /*
    Метод dispose реализует логику уничтожения объекта класса
    Метод ничего не принимает в качестве аргументов
    Метод ничего не возвращает
    */
override fun dispose() {
    yamlMap.clear()
}
```
### ColorRegion.kt
Данный `.kt` файл содержит в себе реализацию открытого класса `ColorRegion`

Конструктор класса содержит следующие аргументы:
```
start <Int> - номер строки начала цветного региона
finish <Int> - номер строки конца цветного региона
color <Color> - цвет цветного региона
```
### HighLighterManager.kt
Данный `.kt` файл содержит в себе реализацию класса `HighLighterManager : Disposable`

Конструктор класса содержит следующие аргументы:
```
project <Project> - объект активного проекта в IDE
```

Класс наследуется от `Disposablse`
#### init
Метод инициализации раскраски файлов в цветные регионы
##### Аргументы
Метод принимает 2 аргумента:
```
e <MutableList<MutableList<ColorRegion>>> - список всех списков регионов для файлов, причём индекс списка регионов связан с файлом индексом
files <MutableList<String>> - список всех путей к найденным в описании файлам
```
##### Возвращаемые значения
Метод ничего не возвращает
##### Реализация
```kotlin
     /*
    Метод init реализует окраску документов, записи о которых нашлись в /.ColorRegion
    Метод принимает 2 аргумента:
    - e (MutableList<MutableList<ColorRegion>>) Список списков ColorRegion-ов для файлов, записи о котрых нашлись
    - files (MutableList<String>>) Список файлов, записи о которых нашлись
    Метод ничего не возвращает
     */
    fun init(e: MutableList<MutableList<ColorRegion>>, files: MutableList<String>) {
        regions = e
        for (i in 0..<regions.size){
            highlighters.add(mutableListOf())
            updateHighlighting(i, files[i])
        }
    }
```
#### updateHighlighting
Метод принудительного обновления раскраски для файла
##### Аргументы
Метод принимает 2 аргумента:
```
ind <Int> - Индекс файла, для которого необходимо обновить раскраску согласно списку цветных регионов для данного файла
file <String> - Путь к файлу, для которого производится принудительное обновление раскраски
```
##### Возвращаемые значения
Метод ничего не возвращает
##### Реализация
```kotlin
     /*
    Метод updateHighlighting реализует повторную окраску одного документа
    Метод принимает 2 аргумента:
    - ind (Int) Номер файла, для которого нужно обновить раскраску
    - file (String) Имя файла, для которого нужно обновить раскраску
    Метод ничего не возвращает
     */
private fun updateHighlighting(ind: Int, file: String){
    for (highLighter in highlighters[ind]){
        highLighter.dispose()
    }
    highlighters[ind].clear()
    val document: Document = FileDocumentManager.getInstance().getDocument(projectDirectory.findFileByRelativePath(file) ?: return) ?: return
    for (region in regions[ind]){
        highlighters[ind].add(makeRangeHighlighter(region, document))
    }
}
```
#### makeRangeHighlighter
Метод создания Highlighter объекта для IDE
##### Аргументы
Метод принимает 2 аргумента:
```
region <ColorRegion> - цветной регион, который необходимо применить к докумету
document <Document> - объект документа, к которому нудно применить раскраску
```
##### Возвращаемые значения
Метод возвращает 1 значение:
```
<RangeHighlighter> - подсветка синтаксиса, необходимая IDE для покраски строки
```
##### Реализация
```kotlin
     /*
    Метод makeRangeHighlighter создаёт объект-Highlighter, который используется IDE для изменения интерфейса
    Метод принимает 2 аргумента:
    - region (ColorRegion) Цветной регион, который необходимо отобразить в интерфейсе
    - document (Document) Объект документа, для которого необходимо отобразить цветной регион
    Метод возвращает объект типа RangeHighlighter - созданный Highlighter для IDE
     */
fun makeRangeHighlighter(region: ColorRegion, document: Document): RangeHighlighter {
    return DocumentMarkupModel.forDocument(document, project, true).addRangeHighlighter(
        document.getLineStartOffset(region.start - 1),
        document.getLineEndOffset(region.finish - 1),
        HighlighterLayer.SELECTION - 1,
        TextAttributes().apply {
            backgroundColor = region.color
        },
        HighlighterTargetArea.LINES_IN_RANGE
    )
}
```
#### dispose
Метод уничтожения класса, который наследуется от `Disposable`
##### Аргументы
Метод ничего не принимает в качестве аргументов
##### Возвращаемые значения
Метод ничего не возвращает
##### Реализация
```kotlin
     /*
    Метод dispose реализует логику уничтожения объекта класса
    Метод ничего не принимает в качестве аргументов
    Метод ничего не возвращает
     */
override fun dispose(){
    for (fileHighlighters in highlighters){
        for (highlighter in fileHighlighters){
            highlighter.dispose()
        }
    }
    highlighters.clear()
    regions.clear()
}
```