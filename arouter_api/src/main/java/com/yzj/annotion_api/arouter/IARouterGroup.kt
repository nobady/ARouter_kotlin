package com.yzj.annotion_api.arouter

/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/20
 *     desc   :
 *     version: 1.0
 * </pre>
 */
interface IARouterGroup {

    /**
     * 比如order模块下有这些信息，personal模块下有这些信息
     * "order" --- IARouterPath的实现类（用APT生成出来的IARouter$$Path$$order）
     * @return key:"order/app/personal"  value:order组下所有的path--class
     */
    fun getGroupMap():Map<String,Class<out IARouterPath>>
}