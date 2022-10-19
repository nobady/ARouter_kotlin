package com.yzj.order

import com.tengfly.arouter.order.R
import com.yzj.annotion_params.annotations.ARouter
import com.yzj.annotion_params.annotations.Provider
import com.yzj.common.order.IOrderDrawable

/**
 * <pre>
 *     @author : Teng Fly
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/10/18
 *     desc   :
 *     package: com.yzj.order
 * </pre>
 */
@Provider("/order/drawable")
class OrderDrawableImpl:IOrderDrawable {
    override fun getLoginDrawableId(): Int {
        return R.drawable.ic_baseline_add_circle_24
    }
}