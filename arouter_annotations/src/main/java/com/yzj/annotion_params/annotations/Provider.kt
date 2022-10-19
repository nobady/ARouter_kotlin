package com.yzj.annotion_params.annotations

/**
 * <pre>
 *     @author : Teng Fly
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/10/18
 *     desc   :
 *     package: com.yzj.annotion_params.annotations
 * </pre>
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Provider(val name:String = "",val group:String = "")
