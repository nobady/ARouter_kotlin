package com.yzj.annotion_compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.yzj.annotion_compiler.utils.ProcessorConfig
import com.yzj.annotion_compiler.utils.ProcessorUtils
import com.yzj.annotion_compiler.utils.TypeUtils
import com.yzj.annotion_compiler.utils.javaToKotlinType
import com.yzj.annotion_params.annotations.Parameter
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/19
 *     desc   :
 *     version: 1.0
 * </pre>
 */
@AutoService(Processor::class)
@SupportedAnnotationTypes(ProcessorConfig.PARAMETER_PACKAGE)
class ParameterProcessor : AbstractProcessor() {

    private lateinit var mFiler: Filer
    private lateinit var mMessager: Messager
    private lateinit var mElementsUtil: Elements
    private lateinit var mTypes: Types
    private lateinit var mTypeUtil: TypeUtils
    private lateinit var iProviderType: TypeMirror

    //存放添加了注解的容器，activity为key，加了注解的参数集合为value
    private val parameterMap = HashMap<TypeElement, List<Element>>()

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        mFiler = processingEnv.filer
        mMessager = processingEnv.messager
        mElementsUtil = processingEnv.elementUtils
        mTypes = processingEnv.typeUtils
        mTypeUtil = TypeUtils(mTypes, mElementsUtil)

        iProviderType = mElementsUtil.getTypeElement(ProcessorConfig.AI_PROVIDER_DIR).asType()
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(elementSet: MutableSet<out TypeElement>, env: RoundEnvironment): Boolean {

        if (elementSet.isEmpty()){
            return false
        }
        mMessager?.printMessage(Diagnostic.Kind.NOTE, "---------->@Parameter注解")
        //扫描到有添加了@Parameter注解的地方
        if (!ProcessorUtils.isEmpty(elementSet)) {
            //找到带有Parameter的注解
            val elements: Set<Element> = env.getElementsAnnotatedWith(Parameter::class.java)
            mMessager?.printMessage(Diagnostic.Kind.NOTE, "elements.size = ${elements.size}")

            //进行分类，activity为key，加了注解的参数集合为value
            elements.forEach {
                //这个就是activity对应的typeElement
                val typeElement = it.enclosingElement as TypeElement
                //如果缓存中存在，直接添加
                if (parameterMap.containsKey(typeElement)) {
                    (parameterMap[typeElement] as ArrayList).add(it)
                } else {
                    val arrayList = ArrayList<Element>()
                    arrayList.add(it)
                    parameterMap[typeElement] = arrayList
                }
            }

            if (ProcessorUtils.isEmpty(parameterMap)) {
                //没有添加注解，直接返回
                return true
            }
            try {
                createParameterFile()
            } catch (e: Exception) {
                mMessager.printMessage(Diagnostic.Kind.NOTE,"ParameterFile 创建失败")
            }
        }
        return true
    }

    private fun createParameterFile() {
        //IParameterGet接口
        val parameterInterfaceType =
            mElementsUtil.getTypeElement(ProcessorConfig.AI_PARAMETER_GET)

        val anyClassType = Any::class.java.asClassName().javaToKotlinType()

        //开始生成文件
        parameterMap.forEach {
            val activityElement = it.key //包含注解的Activity
            val parameterList = it.value  //被Parameter注解标识的参数

            val packageOf = mElementsUtil.getPackageOf(activityElement).qualifiedName.toString()
            val activityName = activityElement.simpleName.toString()
            //生成class
            val parameterClass =
                ClassName(packageOf, "$activityName${ProcessorConfig.PARAMETER_CLASS_NAME}")


            val funcBuild = FunSpec.builder(ProcessorConfig.METHOD_FUNCTION_NAME)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(ProcessorConfig.METHOD_PARAMETER_NAME, anyClassType)
                .addStatement(
                    "val t = %N as %T",
                    ProcessorConfig.METHOD_PARAMETER_NAME,
                    activityElement
                )

            val parameterFactory = ParameterFactory.Builder(funcBuild).setMessager(mMessager).setElementUtil(mElementsUtil).build()

            parameterList.forEach { element->
                if (element.modifiers.contains(Modifier.FINAL)){
                    mMessager.printMessage(Diagnostic.Kind.ERROR,"@Parameter注解不能在val字段上应用，请在${element.enclosingElement}中检查${element.simpleName}字段")
                }
                //如果是IProvider，说明是从其他模块中获取数组，要单独处理
                if (mTypes.isSubtype(element.asType(),iProviderType)){
                    parameterFactory.buildIProviderStatement(element)
                }else{
                    parameterFactory.buildStatement(mTypeUtil.typeExchange(element),element)
                }
            }


            FileSpec.builder(packageOf, "$activityName${ProcessorConfig.PARAMETER_CLASS_NAME}")
                .addType(
                    TypeSpec.classBuilder(parameterClass)
                        .primaryConstructor(FunSpec.constructorBuilder().build())
                        .addSuperinterface(parameterInterfaceType.asClassName())
                        .addFunction(
                            funcBuild.build()
                        )
                        .build()
                ).build().writeTo(mFiler)
        }
    }
}