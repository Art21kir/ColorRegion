package com.art21studio.maybenormalplugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

@OptIn(ExperimentalStdlibApi::class)
@Service(Service.Level.PROJECT)
class HighLighterManager(private val project: Project): Disposable{
    private val highlighters: MutableList<MutableList<RangeHighlighter>> = mutableListOf()
    private val projectDirectory: VirtualFile = project.baseDir!!
    private var regions: MutableList<MutableList<ColorRegion>> = mutableListOf()

    fun init(e: MutableList<MutableList<ColorRegion>>, files: MutableList<String>) {
        regions = e
        for (i in 0..<regions.size){
            highlighters.add(mutableListOf())
            updateHighlighting(i, files[i])
        }
    }
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

    override fun dispose(){
        for (fileHighlighters in highlighters){
            for (highlighter in fileHighlighters){
                highlighter.dispose()
            }
        }
        highlighters.clear()
        regions.clear()
    }

    fun updateRegions(otrezki: MutableList<ColorRegion>, newRegion: ColorRegion): MutableList<ColorRegion> {
        var IN: Boolean = false
        var out: MutableList<ColorRegion> = mutableListOf()
        for (otrezok in otrezki) {
            if (otrezok.finish < newRegion.start || otrezok.start > newRegion.finish) {
                out.add(otrezok)
                continue
            }
            if (newRegion.start <= otrezok.start && otrezok.start <= newRegion.finish) {
                if(!IN){
                    IN = true
                    out.add(newRegion)
                }
                otrezok.start = newRegion.finish + 1
                if (otrezok.start <= otrezok.finish) out.add(otrezok)
                continue
            }
            if (newRegion.start <= otrezok.finish && otrezok.finish <= newRegion.finish){
                otrezok.finish = newRegion.start - 1
                if (otrezok.start <= otrezok.finish) out.add(otrezok)
                if(!IN){
                    IN = true
                    out.add(newRegion)
                }
                continue
            }
            if (otrezok.start < newRegion.start && newRegion.finish < otrezok.finish){
                val new: ColorRegion = ColorRegion(newRegion.finish + 1, otrezok.finish, otrezok.color)
                otrezok.finish = newRegion.start - 1
                out.add(otrezok)
                out.add(newRegion)
                out.add(new)
                IN = true
                continue
            }
        }
        return out
    }

    fun updateRegions(otrezki: MutableList<ColorRegion>, start: Int, finish: Int): MutableList<ColorRegion> {
        var out: MutableList<ColorRegion> = mutableListOf()
        for (otrezok in otrezki) {
            if (otrezok.finish < start || otrezok.start > finish) {
                out.add(otrezok)
                continue
            }
            if (start <= otrezok.start && otrezok.start <= finish) {
                otrezok.start = finish + 1
                if (otrezok.start <= otrezok.finish) out.add(otrezok)
                continue
            }
            if (start <= otrezok.finish && otrezok.finish <= finish){
                otrezok.finish = start - 1
                if (otrezok.start <= otrezok.finish) out.add(otrezok)
                continue
            }
            if (otrezok.start < start && finish < otrezok.finish){
                val new: ColorRegion = ColorRegion(finish + 1, otrezok.finish, otrezok.color)
                otrezok.finish = start - 1
                out.add(otrezok)
                out.add(new)
                continue
            }
        }
        return out
    }
    fun getRegions() : MutableList<MutableList<ColorRegion>>{
        return regions
    }
}