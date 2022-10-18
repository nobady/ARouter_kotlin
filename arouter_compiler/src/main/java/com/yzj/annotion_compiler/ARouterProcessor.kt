package com.yzj.annotion_compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.yzj.annotion_compiler.utils.ProcessorConfig
import com.yzj.annotion_compiler.utils.javaToKotlinType
import com.yzj.annotion_params.AnnotationBean
import com.yzj.annotion_params.RouterBean
import com.yzj.annotion_params.TypeEnum
import com.yzj.annotion_params.annotations.ARouter
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.FileObject
import kotlin.reflect.KClass

/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/20
 *     desc   :
 *     version: 1.0
 * </pre>
 */
@AutoService(Processor::class)
@SupportedAnnotationTypes(ProcessorConfig.AROUTER_PACKAGE)
class ARouterProcessor : AbstractProcessor() {

    //生成文件
    private lateinit var mFiler: Filer

    //打印日志
    private lateinit var mMessager: Messager

    //操作类的工具
    private lateinit var mElementsUtil: Elements

    //
    private lateinit var mTypes: Types

    private lateinit var mPathMap: HashMap<String, ArrayList<AnnotationBean>>
    private lateinit var mGroupMap: HashMap<String, ClassName>

    //android.app.Activity的类型
    private lateinit var activityTypeMirror: TypeMirror


    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        mFiler = processingEnv.filer
        mMessager = processingEnv.messager
        mElementsUtil = processingEnv.elementUtils
        mTypes = processingEnv.typeUtils


        val activityElement = mElementsUtil.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE)
        activityTypeMirror = activityElement.asType()
        mPathMap = HashMap()
        mGroupMap = HashMap()
    }


    override fun process(set: MutableSet<out TypeElement>, env: RoundEnvironment): Boolean {

        if (set.isEmpty()) {
            return false
        }

        mMessager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>ARouter注解")

        //使用javapoet的方式是倒叙的方式编写代码，OOP思想
        //传统思想  包   类   方法
        //javapoet  方法   类   包

        //找到所有被ARouter注解标识的类
        val arouterSet = env.getElementsAnnotatedWith(ARouter::class.java)
        arouterSet.takeIf { it.isNotEmpty() }?.run {
            forEach {
                val activityName = it.simpleName.toString()
                //获取到ARouter注解
                val aRouterAnnotation = it.getAnnotation(ARouter::class.java)
                //获取注解中的path
                val path = aRouterAnnotation.path
                //获取注解中的group
                var group = aRouterAnnotation.group
                //进行安全性判断，为空抛异常
                if (path.isEmpty()) {
                    mMessager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "$activityName 中@ARouter注解的path不能为空"
                    )
                    return true
                }
                if (path.indexOfFirst { it == '/' } != 0 ||
                    path.indexOfLast { it == '/' } == 0 ||
                    path.indexOfLast { it == '/' } == path.length
                ) {
                    mMessager.printMessage(
                        Diagnostic.Kind.ERROR,"请传入正确的path路径：eg：/app/main")
                    return true
                }

                if (group.isEmpty()) {
                    //直接从path中取
                   group = path.split("/")[1]
                }
                mMessager.printMessage(
                    Diagnostic.Kind.NOTE,
                    "$activityName 中@ARouter注解的group = $group"
                )
                if (!mTypes.isSubtype(it.asType(), activityTypeMirror)) {
                    mMessager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "$activityName  中@ARouter注解只能作用于Activity之上"
                    )
                    return true
                }
                //构建RouteBean，用来将group、path、class信息绑定起来
                val annotationBean = AnnotationBean(
                    TypeEnum.ACTIVITY,
                    it,
                    path,
                    group
                )
                //通过map将group和path关联起来
                val routerBeanList = mPathMap[group]
                if (routerBeanList == null || routerBeanList.isEmpty()) {
                    val list = ArrayList<AnnotationBean>()
                    list.add(annotationBean)
                    mPathMap[group] = list
                } else {
                    if (routerBeanList.contains(annotationBean)){
                        routerBeanList.find { bean -> bean==annotationBean }?.let { bean ->
                            mMessager.printMessage(
                                Diagnostic.Kind.ERROR,
                                "${bean.element}  中@ARouter注解的path和group与${it}中@ARouter注解一致"
                            )
                            return true
                        }
                    }
                    routerBeanList.add(annotationBean)
                }
            }
        }
        //找到group和path的关联信息之后，创建文件
        try {
            createPathFile()
        } catch (e: Exception) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "path文件写入异常:${e.message}")
        }

        try {
            createGroupFile()
        } catch (e: Exception) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "group文件写入异常:${e.message}")
        }

        try {
            createConstFile()
        } catch (e: Exception) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "const文件写入异常:${e.message}")
        }

        return true
    }

    /**
     * TODO 将group和path封装成常量，方便代码中调用
     * 根据组名来创建文件，
     */
    private fun createConstFile() {
        //所有的path存放在这个文件中
        val fileSpec = FileSpec.builder(ProcessorConfig.AROUTER_CONSTANT_DIR, ProcessorConfig.CLASS_AROUTER_CONSTANT_PRE)
        mPathMap.forEach {
            val group = it.key
            val groupClassName = ClassName(ProcessorConfig.AROUTER_CONSTANT_DIR, group)

            val classBuilder = TypeSpec.objectBuilder(groupClassName)
            mMessager.printMessage(Diagnostic.Kind.NOTE, "开始创建Constant文件:$groupClassName")
            it.value.forEach { bean ->
                //  /app/main的格式，要变成main
                var path = bean.path!!
                path.indexOfLast { it == '/' }.let {
                    path = path.substring(it + 1)
                }
                classBuilder.addProperty(
                    PropertySpec.builder(
                        path.uppercase(),
                        String::class.java.asTypeName().javaToKotlinType(),
                        KModifier.CONST
                    ).initializer("%S", "${bean.path}").build()
                )
            }
            fileSpec.addType(classBuilder.build())
        }
        fileSpec.build().writeTo(mFiler)

    }

    private fun createGroupFile() {
        mGroupMap.forEach {
            val group = it.key
            //生成group与path的对应类
            val groupClassName = ClassName(ProcessorConfig.AROUTER_GROUP_DIR, "${ProcessorConfig.CLASS_AROUTER_GROUP_PRE}$group")
            //Class<out IARouterPath>
            val classType = Class::class.java.asClassName().parameterizedBy(
                WildcardTypeName.producerOf(
                    mElementsUtil.getTypeElement(ProcessorConfig.AI_AROUTER_PATH).asClassName()
                )
            ).javaToKotlinType()

            //Map<String,Class<out IARouterPath>>
            val groupReturnType = Map::class.java.asClassName().parameterizedBy(
                String::class.java.asTypeName().javaToKotlinType(),
                classType
            ).javaToKotlinType()
            val pathClassName = ClassName(ProcessorConfig.AROUTER_PATH_DIR, "${ProcessorConfig.CLASS_AROUTER_PATH_PRE}$group")

            FileSpec.builder(ProcessorConfig.AROUTER_GROUP_DIR, "${ProcessorConfig.CLASS_AROUTER_GROUP_PRE}$group")
                .addType(
                    TypeSpec.classBuilder(groupClassName)
                        .primaryConstructor(FunSpec.constructorBuilder().build())
                        .addSuperinterface(
                            mElementsUtil.getTypeElement(ProcessorConfig.AI_AROUTER_GROUP)
                                .asClassName()
                        )
                        .addFunction(
                            FunSpec.builder("getGroupMap")
                                .addModifiers(KModifier.OVERRIDE)
                                .returns(groupReturnType)
                                //val groupMap = HashMap<String,>
                                .addStatement(
                                    "val groupMap = mutableMapOf<%T,%T>()",
                                    String::class.java.asTypeName().javaToKotlinType(),
                                    classType
                                )
                                //groupMap["group"] = ARouter$$Path$$group::class.java
                                .addStatement("groupMap[%S] = %T::class.java", group, pathClassName)
                                .addStatement("return groupMap")
                                .build()
                        )
                        .build()
                ).build().writeTo(mFiler)
        }

    }

    /**
     * TODO 需要用代码创建如下
     */
    private fun createPathFile() {

        mPathMap.forEach { it ->
            val group = it.key
            //返回值类型  Map<String,RouteBean>
            val returnType = Map::class.java.asClassName().parameterizedBy(
                String::class.java.asTypeName().javaToKotlinType(),
                RouterBean::class.java.asTypeName()
            ).javaToKotlinType()
            //构建方法体
            val funcSpec = FunSpec.builder("getPathMap")   //方法名
                .returns(returnType)   //添加返回类型
                .addModifiers(KModifier.OVERRIDE)
                .addStatement(
                    "val map = mutableMapOf<%T,%T>()",    //编写代码
                    String::class.java.asTypeName().javaToKotlinType(),
                    RouterBean::class.java.asTypeName()
                )
            //一个模块下会有多个path，所以这里需要循环遍历，
            it.value.forEach { bean ->
                funcSpec.addStatement(
                    "map[%S] = %T().apply{ \n" +
                            "path = %S\n" +
                            "group = %S\n" +
                            "aClass = %T::class.java \n" +
                            "typeEnum = %T.%L \n" +
                            "}",
                    "${bean.path}",
                    RouterBean::class.java,
                    "${bean.path}",
                    "${bean.group}",
                    bean.element!!.asType().asTypeName(),
                    TypeEnum::class.java,
                    bean.typeEnum!!
                )
            }
            funcSpec.addStatement("return map")   //  返回值

            //------------生成IARouterPath的实现类
            //类名
            val className = ClassName(ProcessorConfig.AROUTER_PATH_DIR, "${ProcessorConfig.CLASS_AROUTER_PATH_PRE}$group")
            //文件名
            FileSpec.builder(ProcessorConfig.AROUTER_PATH_DIR, "${ProcessorConfig.CLASS_AROUTER_PATH_PRE}$group")
                .addType(
                    TypeSpec.classBuilder(className)   //开始创建类
                        .primaryConstructor(FunSpec.constructorBuilder().build())   //无参构造函数
                        .addSuperinterface(    //实现的接口
                            ClassName.bestGuess(
                                mElementsUtil.getTypeElement(
                                    ProcessorConfig.AI_AROUTER_PATH
                                ).toString()
                            )
                        )
                        .addFunction(
                            funcSpec.build()   //方法构建
                        )
                        .build()
                ).build().writeTo(mFiler)

            mGroupMap[group] = className

        }
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}