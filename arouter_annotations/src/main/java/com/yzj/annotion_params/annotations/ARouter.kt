package com.yzj.annotion_params.annotations

/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/20
 *     desc   :
 *     version: 1.0
 * </pre>
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ARouter(val path:String,val group:String = "")
