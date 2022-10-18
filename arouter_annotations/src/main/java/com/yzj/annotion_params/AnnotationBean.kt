package com.yzj.annotion_params

import javax.lang.model.element.Element

/**
 * <pre>
 *     @author : Teng Fly
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/10/17
 *     desc   :
 *     package: com.yzj.annotion_params
 * </pre>
 */
class AnnotationBean(
    @JvmField
    var typeEnum: TypeEnum? = null,
    @JvmField
    var element: Element? = null,
    @JvmField
    var path: String? = null,
    @JvmField
    var group: String? = null
){

    override fun equals(other: Any?): Boolean {
        return if (other is AnnotationBean){
            other.path.equals(path)&&other.group.equals(group)
        }else{
            super.equals(other)
        }

    }
}