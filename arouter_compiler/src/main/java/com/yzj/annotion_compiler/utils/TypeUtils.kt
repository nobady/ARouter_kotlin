package com.yzj.annotion_compiler.utils

import com.yzj.annotion_params.utils.TypeKind
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/20
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class TypeUtils(types: Types,elements: Elements) {


    private val mTypes = types
    private val parcelableType = elements.getTypeElement(ProcessorConfig.PARCELABLE).asType()
    private val serializableType = elements.getTypeElement(ProcessorConfig.SERIALIZABLE).asType()

    fun typeExchange(element: Element):Int{
        val typeMirror = element.asType()

        if (typeMirror.kind.isPrimitive){
            return element.asType().kind.ordinal
        }

        when(typeMirror.toString()){
            ProcessorConfig.BYTE -> return TypeKind.BYTE.ordinal
            ProcessorConfig.SHORT -> return TypeKind.SHORT.ordinal
            ProcessorConfig.INTEGER -> return TypeKind.INT.ordinal
            ProcessorConfig.LONG -> return TypeKind.LONG.ordinal
            ProcessorConfig.FLOAT -> return TypeKind.FLOAT.ordinal
            ProcessorConfig.DOUBEL -> return TypeKind.DOUBLE.ordinal
            ProcessorConfig.STRING -> return TypeKind.STRING.ordinal
            else ->{
                return if (mTypes.isSubtype(typeMirror,parcelableType)){
                    TypeKind.PARCELABLE.ordinal
                }else if (mTypes.isSubtype(typeMirror,serializableType)){
                    TypeKind.SERIALIZABLE.ordinal
                }else{
                    TypeKind.OBJECT.ordinal
                }
            }
        }
    }
}