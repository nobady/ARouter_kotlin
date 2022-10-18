package com.yzj.annotion_params.annotations


/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/19
 *     desc   :
 *     version: 1.0
 * </pre>
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Parameter(val name:String = "")
