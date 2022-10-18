package com.yzj.annotion_api

import android.app.Activity
import android.util.LruCache


/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/20
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object ParameterManager {


    private const val PARAMETER_CLASS_NAME = "$$"+"_Parameter"
    private val cache: LruCache<String, IParameterGet> by lazy { LruCache<String,IParameterGet>(100) }


    fun loadParameter(activity: Activity){
        //因为我们生成的类的格式是activity.name+$$Paramter,所以这里需要拼接一下
        val className = activity.javaClass.name

        var iParameterGet = cache.get(className)
        if (iParameterGet==null){
            //缓存中没有，新增
            val targetClassName = "${className}$PARAMETER_CLASS_NAME"
            //通过反射拿到类对象
            val anyClass = Class.forName(targetClassName)
            iParameterGet = anyClass.newInstance() as IParameterGet
            //加入到缓存中
            cache.put(className,iParameterGet)
        }
        //执行参数赋值
        iParameterGet.getParameter(activity)

    }
}