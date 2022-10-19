package com.yzj.annotion_params

import javax.lang.model.element.Element

/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/20
 *     desc   :  封装被ARouter标识的类
 *     version: 1.0
 * </pre>
 */
class RouterBean(
    @JvmField
    var typeEnum: TypeEnum? = null,
    @JvmField
    var aClass: Class<*>? = null,
    @JvmField
    var path: String? = null,
    @JvmField
    var group: String? = null
)

enum class TypeEnum {
    ACTIVITY,
    FRAGMENT,
    PROVIDER
}
