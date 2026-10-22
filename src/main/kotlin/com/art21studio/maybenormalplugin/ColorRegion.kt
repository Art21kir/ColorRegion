package com.art21studio.maybenormalplugin

import java.awt.Color

open class ColorRegion(var start: Int, var finish: Int, var color: Color){
    override fun toString() : String{
        return "$color $start $finish"
    }
}