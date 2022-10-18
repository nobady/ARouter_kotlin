package com.yzj.annotion_compiler.utils

/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/19
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object ProcessorUtils {

    fun isEmpty(charSequence: CharSequence): Boolean {
        return charSequence.isEmpty()
    }

    fun isEmpty(collection: Collection<Any>): Boolean {
        return collection.isEmpty()
    }

    fun isEmpty(map: Map<*,*>):Boolean{
        return map.isEmpty()
    }
}