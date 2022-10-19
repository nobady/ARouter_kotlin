package com.yzj.common.order

import com.yzj.annotion_api.IProvider

/**
 * <pre>
 *     @author : Teng Fly
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/10/18
 *     desc   : order模块对外暴露的接口，提供给可供其他模块访问的图片资源,具体的实现由order模块来实现
 *     package: com.yzj.common.order
 * </pre>
 */
interface IOrderDrawable:IProvider {

    fun getLoginDrawableId():Int
}