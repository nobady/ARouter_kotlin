package com.yzj.annotion_api.arouter

import com.yzj.annotion_params.RouterBean

/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/20
 *     desc   :
 *     version: 1.0
 * </pre>
 */
interface IARouterPath {
    /**
     * 存放path和class的对应关系
     * @return key:"app/MainActivity" value:RouterBean  ,其中bean中的class对应的就是MainActivity.class
     */
    fun getPathMap():Map<String,RouterBean>

}