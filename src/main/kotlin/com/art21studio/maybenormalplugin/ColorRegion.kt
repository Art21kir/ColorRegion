package com.art21studio.maybenormalplugin

import java.awt.Color

/*
Открытый класс ColorRegion для хранения основынх параметров абстрактного цветового региона
Конструктор класса состоит из трёх параметров:
- start (Int) Номер строки начала цветного региона
- end (Int) Номер строки конца цветного региона (включительно)
- color (java.awt.Color) Цвет региона
 */
open class ColorRegion(var start: Int, var finish: Int, var color: Color){
    /*
    Метод toString объясняет приведение объекта типа ColorRegion в String
    Метод ничего не принимает в качестве аргументов
    Метод возвращает String с записанными подряд параметрами ColorRegion
     */
    override fun toString() : String{
        return "$color $start $finish"
    }
}