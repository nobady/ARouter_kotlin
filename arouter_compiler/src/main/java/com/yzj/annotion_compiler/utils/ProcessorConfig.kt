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
object ProcessorConfig {

    const val ACTIVITY_PACKAGE = "android.app.Activity"
    const val PARAMETER_PACKAGE = "com.yzj.annotion_params.annotations.Parameter"

    const val AROUTER_PACKAGE = "com.yzj.annotion_params.annotations.ARouter"

    const val PARAMETER_CLASS_NAME = "$\$_Parameter"
    const val AROUTER_CLASS_NAME = "$\$_ARouter"

    const val PARAMETER_API_PACKAGE = "com.yzj.annotion_api"
    const val AI_PARAMETER_GET = "${PARAMETER_API_PACKAGE}.IParameterGet"
    const val AI_AROUTER_PATH = "${PARAMETER_API_PACKAGE}.arouter.IARouterPath"
    const val AI_AROUTER_GROUP = "${PARAMETER_API_PACKAGE}.arouter.IARouterGroup"


    const val ROOT_AROUTER_PACKAGE = "com.tengfly.arouter"
    const val AROUTER_PATH_DIR = "${ROOT_AROUTER_PACKAGE}.path"
    const val AROUTER_GROUP_DIR = "${ROOT_AROUTER_PACKAGE}.group"
    const val AROUTER_CONSTANT_DIR = "${ROOT_AROUTER_PACKAGE}.constant"

    const val CLASS_AROUTER_GROUP_PRE = "ARouter$\$Group$\$"
    const val CLASS_AROUTER_PATH_PRE = "ARouter$\$Path$\$"
    const val CLASS_AROUTER_CONSTANT_PRE = "Constant"


    const val METHOD_PARAMETER_NAME = "targetParameter"
    const val METHOD_FUNCTION_NAME = "getParameter"


    // Java type
    private const val LANG = "java.lang"
    const val BYTE = "$LANG.Byte"
    const val SHORT = "$LANG.Short"
    const val INTEGER = "$LANG.Integer"
    const val LONG = "$LANG.Long"
    const val FLOAT = "$LANG.Float"
    const val DOUBEL = "$LANG.Double"
    const val BOOLEAN = "$LANG.Boolean"
    const val CHAR = "$LANG.Character"
    const val STRING = "$LANG.String"
    const val SERIALIZABLE = "java.io.Serializable"
    const val PARCELABLE = "android.os.Parcelable"

}