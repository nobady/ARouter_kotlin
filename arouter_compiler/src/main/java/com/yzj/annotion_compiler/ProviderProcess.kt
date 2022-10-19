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
import com.yzj.annotion_params.annotations.Provider
import javax.annotation.processing.*
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * <pre>
 *     @author : Teng Fly
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/10/18
 *     desc   :
 *     package: com.yzj.annotion_compiler
 * </pre>
 */
@AutoService(Processor::class)
@SupportedAnnotationTypes(ProcessorConfig.PROVIDER_PACKAGE)
class ProviderProcess : AbstractProcessor() {


    //生成文件
    private lateinit var mFiler: Filer

    //打印日志
    private lateinit var mMessager: Messager

    //操作类的工具
    private lateinit var mElementsUtil: Elements

    //
    private lateinit var mTypes: Types

    private lateinit var providerTypeMirror: TypeMirror

    private lateinit var mPathMap: HashMap<String, ArrayList<AnnotationBean>>
    private lateinit var mGroupMap: HashMap<String, ClassName>


    override fun init(processingEnv: ProcessingEnvironment) {
        mFiler = processingEnv.filer
        mMessager = processingEnv.messager
        mElementsUtil = processingEnv.elementUtils
        mTypes = processingEnv.typeUtils

        val providerElement = mElementsUtil.getTypeElement(ProcessorConfig.AI_PROVIDER_DIR)
        providerTypeMirror = providerElement.asType()
        mPathMap = HashMap()
        mGroupMap = HashMap()
    }

    override fun process(set: MutableSet<out TypeElement>, env: RoundEnvironment): Boolean {
        if (set.isEmpty()) return true

        mMessager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>Provider注解")

        val providerSet = env.getElementsAnnotatedWith(Provider::class.java)

        providerSet.forEach {

            val classNameStr = it.simpleName.toString()
            val annotation = it.getAnnotation(Provider::class.java)
            val name = annotation.name
            var group = annotation.group

            if (name.isEmpty()) {
                mMessager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "$classNameStr 中@Provider注解的name不能为空"
                )
                return true
            }
            if (name.indexOfFirst { it == '/' } != 0 ||
                name.indexOfLast { it == '/' } == 0 ||
                name.indexOfLast { it == '/' } == name.length
            ) {
                mMessager.printMessage(
                    Diagnostic.Kind.ERROR, "请传入正确的path路径：eg：/app/main"
                )
                return true
            }

            if (group.isEmpty()) {
                //直接从path中取
                group = name.split("/")[1]
            }

            //判断类型
            if (!mTypes.isSubtype(it.asType(), providerTypeMirror)) {
                //如果没有实现IProvider接口，就报错
                mMessager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "由于$it 添加了@Provider注解所以需要实现IProvider接口"
                )
            }

            val annotationBean = AnnotationBean().apply {
                this.group = group
                this.path = name
                this.element = it
                this.typeEnum = TypeEnum.PROVIDER
            }

            val pathBeanList = mPathMap[group]
            if (pathBeanList == null) {
                val list = ArrayList<AnnotationBean>()
                list.add(annotationBean)
                mPathMap[group] = list
            } else {
                pathBeanList.add(annotationBean)
            }


            //创建文件
            try {
                createPathFile()
            } catch (e: Exception) {
                mMessager.printMessage(Diagnostic.Kind.ERROR,"创建Provider Path文件失败")
            }

            try {
                createGroupFile()
            } catch (e: Exception) {
                mMessager.printMessage(Diagnostic.Kind.ERROR,"创建Provider Group文件失败")
            }
            try {
                createConstFile()
            } catch (e: Exception) {
                mMessager.printMessage(Diagnostic.Kind.ERROR,"创建Provider Const文件失败")
            }

        }

        return true
    }

    private fun createConstFile() {
        //所有的path存放在这个文件中
        val fileSpec = FileSpec.builder(ProcessorConfig.AROUTER_PROVIDER_DIR, ProcessorConfig.CLASS_AROUTER_CONSTANT_PRE)
        mPathMap.forEach {
            val group = it.key
            val groupClassName = ClassName(ProcessorConfig.AROUTER_PROVIDER_DIR, group)

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

    private fun createPathFile() {
        mPathMap.forEach {
            val group = it.key

            val providerPathClass = ClassName(
                ProcessorConfig.AROUTER_PROVIDER_DIR,
                "${ProcessorConfig.CLASS_PROVIDER_PATH_PRE}$group"
            )

            //Map<String, RouterBean>
            val pathReturnType = Map::class.java.asClassName().parameterizedBy(
                String::class.java.asTypeName().javaToKotlinType(),
                RouterBean::class.java.asTypeName()
            ).javaToKotlinType()

            val pathFuncSpec = FunSpec.builder(ProcessorConfig.FUNCTION_IPATH_NAME)
                .addModifiers(KModifier.OVERRIDE)
                .returns(pathReturnType)
                .addStatement(
                    "val pathMap = mutableMapOf<%T,%T>()",
                    String::class.java.asTypeName().javaToKotlinType(),
                    RouterBean::class.java.asTypeName()
                )
            // map["/app/main"] = RouterBean().apply{
            //        path = "/app/main"
            //        group = "app"
            //        aClass = MainActivity::class.java
            //        typeEnum = TypeEnum.ACTIVITY
            //        }
            it.value.forEach { bean ->
                pathFuncSpec.addStatement(
                    "pathMap[%S] = %T().apply{ \n " +
                            "path = %S \n" +
                            "group = %S \n" +
                            "aClass = %T::class.java \n" +
                            "typeEnum = %T.%L \n" +
                            "}",
                    "${bean.path}",
                    RouterBean::class.java,
                    "${bean.path}",
                    "${bean.group}",
                    bean.element!!,
                    TypeEnum::class.java,
                    bean.typeEnum!!
                )
            }
            pathFuncSpec.addStatement("return pathMap")

            FileSpec.builder(
                ProcessorConfig.AROUTER_PROVIDER_DIR,
                "${ProcessorConfig.CLASS_PROVIDER_PATH_PRE}$group"
            )
                .addType(
                    TypeSpec.classBuilder(providerPathClass)
                        .primaryConstructor(FunSpec.constructorBuilder().build())
                        .addSuperinterface(
                            mElementsUtil.getTypeElement(ProcessorConfig.AI_AROUTER_PATH)
                                .asClassName()
                        )
                        .addFunction(
                            pathFuncSpec.build ()
                )
                .build())
            .build().writeTo(mFiler)

            mGroupMap[group] = providerPathClass

        }
    }

    private fun createGroupFile(){
        mGroupMap.forEach {
            val group = it.key

            val providerGroupClass = ClassName(
                ProcessorConfig.AROUTER_PROVIDER_DIR,
                "${ProcessorConfig.CLASS_PROVIDER_GROUP_PRE}$group"
            )
            //Class<out IARouterPath
            val classType = Class::class.java.asClassName().parameterizedBy(
                WildcardTypeName.producerOf(mElementsUtil.getTypeElement(ProcessorConfig.AI_AROUTER_PATH).asType().asTypeName())
            ).javaToKotlinType()
            //Map<String,Class<out IARouterPath>>
            val groupReturnType = Map::class.java.asClassName().parameterizedBy(
                String::class.java.asTypeName().javaToKotlinType(),
                classType
            ).javaToKotlinType()

            val groupFunSpec = FunSpec.builder(ProcessorConfig.FUNCTION_IGROUP_NAME)
                .addModifiers(KModifier.OVERRIDE)
                .returns(groupReturnType)
                .addStatement("val groupMap = mutableMapOf<%T,%T>()",
                    String::class.java.asTypeName().javaToKotlinType(),
                    classType
                ).addStatement("groupMap[%S] = %T::class.java", group,it.value)
                .addStatement("return groupMap")

            FileSpec.builder(  ProcessorConfig.AROUTER_PROVIDER_DIR,
                "${ProcessorConfig.CLASS_PROVIDER_GROUP_PRE}$group")
                .addType(
                    TypeSpec.classBuilder(providerGroupClass)
                        .primaryConstructor(FunSpec.constructorBuilder().build())
                        .addSuperinterface(mElementsUtil.getTypeElement(ProcessorConfig.AI_AROUTER_GROUP).asClassName())
                        .addFunction(groupFunSpec.build())
                        .build()
                )
                .build().writeTo(mFiler)
        }
    }


}