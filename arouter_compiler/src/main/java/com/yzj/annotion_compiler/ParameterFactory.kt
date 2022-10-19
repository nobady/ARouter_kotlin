package com.yzj.annotion_compiler

import com.squareup.kotlinpoet.*
import com.yzj.annotion_compiler.utils.ProcessorUtils
import com.yzj.annotion_compiler.utils.javaToKotlinType
import com.yzj.annotion_params.annotations.Parameter
import com.yzj.annotion_params.utils.TypeKind
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.Messager
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/19
 *     desc   :
目的 生成以下代码：
@Override
public void getParameter(Object targetParameter) {
Personal_MainActivity t = (Personal_MainActivity) targetParameter;
t.name = t.getIntent().getStringExtra("name");
t.sex = t.getIntent().getStringExtra("sex");
}
 *     version: 1.0
 * </pre>
 */
class ParameterFactory private constructor(builder: Builder) {

    private var messager: Messager? = null

    //    private var method: MethodSpec.Builder? = null
    private var className: ClassName? = null
    private var funcBuild: FunSpec.Builder
    private var elementsUtil: Elements? = null


    init {
        this.className = builder.className
        this.messager = builder.messager
        this.elementsUtil = builder.elementsUtil
        funcBuild = builder.mFuncBuild
    }

    fun buildIProviderStatement( element: Element){
        //被注解标识的参数名称  eg:String name  这个filedName就是name
        val filedName = element.simpleName.toString()
        val annotionValue = element.getAnnotation(Parameter::class.java).name
        if (ProcessorUtils.isEmpty(annotionValue)){
            messager?.printMessage(Diagnostic.Kind.ERROR,"$element 的@Parameter注解必须设置name")
            return
        }
        val typeMirror = elementsUtil!!.getTypeElement("com.yzj.annotion_api.ProviderManager").asType()
        //t.drawable = ProviderManager.build().navigation()
        funcBuild.addStatement("t.$filedName = %T.build(%S).navigation() as ${element.asType()}",typeMirror,annotionValue)

    }

    //生成t.name = t.getIntent().getStringExtra("name");
    fun buildStatement(type: Int, element: Element) {

        //被注解标识的参数名称  eg:String name  这个filedName就是name
        val filedName = element.simpleName.toString()
        //获取注解设置的值
        var annotionValue = element.getAnnotation(Parameter::class.java).name
        //判断一下，如果没有设置注解的值，就取filedName
        annotionValue = if (ProcessorUtils.isEmpty(annotionValue)) filedName else annotionValue

        //主要是针对String类型的参数，因为getStringExtra 返回值是可空的，如果字段是可空，那么不会有问题，如果字段不是可空，那么这里就需要兼容
        var isCanNull = true
        element.annotationMirrors.forEach {
            if (it.annotationType.getAnnotation(Nullable::class.java) == null) {
                isCanNull = false
                return@forEach
            }
        }

        val sb = StringBuffer()

        sb.append("t.$filedName = t.intent.")

        buildStatement(sb, annotionValue, filedName, type, isCanNull, element)

        funcBuild.addStatement(sb.toString())
    }

    private fun buildStatement(
        stringBuffer: StringBuffer,
        parameterValueKey: String,
        originalValue: String,
        type: Int,
        isCanNull: Boolean,
        element: Element
    ) {
        when (type) {
            TypeKind.INT.ordinal -> stringBuffer.append("getIntExtra(\"$parameterValueKey\",t.$originalValue)")
            TypeKind.BOOLEAN.ordinal -> stringBuffer.append("getBooleanExtra(\"$parameterValueKey\",t.$originalValue)")
            TypeKind.CHAR.ordinal -> stringBuffer.append("getCharExtra(\"$parameterValueKey\",t.$originalValue)")
            TypeKind.SHORT.ordinal -> stringBuffer.append("getShortExtra(\"$parameterValueKey\",t.$originalValue)")
            TypeKind.LONG.ordinal -> stringBuffer.append("getLongExtra(\"$parameterValueKey\",t.$originalValue)")
            TypeKind.FLOAT.ordinal -> stringBuffer.append("getFloatExtra(\"$parameterValueKey\",t.$originalValue)")
            TypeKind.DOUBLE.ordinal -> stringBuffer.append("getDoubleExtra(\"$parameterValueKey\",t.$originalValue)")
            TypeKind.BYTE.ordinal -> stringBuffer.append("getByteExtra(\"$parameterValueKey\",t.$originalValue)")
            TypeKind.STRING.ordinal -> {
                if (!isCanNull) {
                    stringBuffer.setLength(0)
                    stringBuffer.append("t.$originalValue = ")
                    stringBuffer.append("if(t.intent.getStringExtra(\"$parameterValueKey\")==null) t.$originalValue else t.intent.getStringExtra(\"$parameterValueKey\")!!")
                } else {
                    stringBuffer.append("getStringExtra(\"$parameterValueKey\")")
                }
            }
            TypeKind.SERIALIZABLE.ordinal, TypeKind.OBJECT.ordinal -> {
                stringBuffer.setLength(0)
                stringBuffer.append("t.$originalValue = ")
                stringBuffer.append("if(t.intent.getSerializableExtra(\"$parameterValueKey\")==null) t.$originalValue else t.intent.getSerializableExtra(\"$parameterValueKey\")!! as ${element.asType()}")
            }
            TypeKind.PARCELABLE.ordinal -> {
                stringBuffer.setLength(0)
                stringBuffer.append("t.$originalValue = ")
                stringBuffer.append("if(t.intent.getParcelableExtra(\"$parameterValueKey\")==null) t.$originalValue else t.intent.getParcelableExtra(\"$parameterValueKey\")!! as ${element.asType()}")
            }
        }
    }


    class Builder(funcBuild: FunSpec.Builder) {
        var messager: Messager? = null
        var className: ClassName? = null
        var elementsUtil: Elements? = null
        var mFuncBuild: FunSpec.Builder

        init {
            mFuncBuild = funcBuild
        }

        fun setClassName(className: ClassName): Builder {
            this.className = className
            return this
        }

        fun setMessager(messager: Messager): Builder {
            this.messager = messager
            return this
        }

        fun build(): ParameterFactory {
            return ParameterFactory(this)
        }

        fun setElementUtil(elementsUtil: Elements): Builder {
            this.elementsUtil = elementsUtil
            return this
        }


    }
}