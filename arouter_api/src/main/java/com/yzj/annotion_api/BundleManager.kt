package com.yzj.annotion_api

import android.content.Context
import android.os.Bundle

/**
 * <pre>
 *     @author : Teng Fly
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/10/17
 *     desc   :   todo 用于管理参数传递
 *     package: com.yzj.annotion_api
 * </pre>
 */
class BundleManager {
    //activity传参实际用的bundle，这里也用bundle管理
    val bundle by lazy { Bundle() }

    fun withString(key:String,parmas:String): BundleManager {
        bundle.putString(key,parmas)
        return this
    }

    fun withInt(key:String,parmas:Int): BundleManager {
        bundle.putInt(key,parmas)
        return this
    }

    fun withBoolean(key:String,parmas:Boolean): BundleManager {
        bundle.putBoolean(key,parmas)
        return this
    }

    fun withBundle(key:String,parmas:Bundle): BundleManager {
        bundle.putBundle(key,parmas)
        return this
    }

    fun navigation(context:Context) = RouterManager.navigation(context,this)
}