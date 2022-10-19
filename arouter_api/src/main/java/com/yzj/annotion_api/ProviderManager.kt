package com.yzj.annotion_api

import android.content.Context
import android.content.Intent
import android.util.LruCache
import com.yzj.annotion_api.arouter.IARouterGroup
import com.yzj.annotion_api.arouter.IARouterPath
import java.lang.reflect.Method

/**
 * <pre>
 *     @author : Teng Fly
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/10/17
 *     desc   : todo 1.查找group  2.查找path
 *     package: com.yzj.annotion_api
 * </pre>
 */
object ProviderManager {

    private var group: String = ""
    private var path: String = ""

    private val groupLruCache by lazy { LruCache<String, IARouterGroup>(100) }
    private val pathLruCache by lazy { LruCache<String, IARouterPath>(100) }


    fun build(path: String): ProviderManager {
        if (path.indexOfFirst { it == '/' } != 0 ||
            path.indexOfLast { it == '/' } == 0 ||
            path.indexOfLast { it == '/' } == path.length
        ) {
            throw RuntimeException("请传入正确的path路径：eg：/app/main")
        }
        this.group = path.split("/")[1]
        this.path = path
        return this
    }


    /**
     * TODO 寻址
     *
     * @param context  上下文
     * @param bundleManager   参数管理器
     * @return
     */
    fun navigation(): IProvider {
        //todo 根据group找到管理组的类   ARouter$$Group$${group}
        var groupClassName = groupLruCache.get(group)
        // 缓存中没有找到，反射找到
        if (groupClassName == null) {
            val clazz = Class.forName("com.tengfly.arouter.provider.Provider$\$Group$\$${group}")
            groupClassName = clazz.newInstance() as IARouterGroup
            groupLruCache.put(group, groupClassName)
        }

        if (groupClassName.getGroupMap().isEmpty()) {
            throw RuntimeException("group路由表加载失败")
        }

        var pathClassName = pathLruCache.get(path)
        if (pathClassName == null) {
            pathClassName = groupClassName.getGroupMap()[group]?.newInstance() as IARouterPath
            pathLruCache.put(path, pathClassName)
        }
        if (pathClassName.getPathMap().isEmpty()) {
            throw RuntimeException("path路由表加载失败")
        }
        return pathClassName.getPathMap()[path]!!.aClass!!.newInstance() as IProvider
    }
}