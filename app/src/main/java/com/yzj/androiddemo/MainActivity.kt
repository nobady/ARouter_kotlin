package com.yzj.androiddemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.tengfly.arouter.constant.order
import com.tengfly.arouter.constant.personal
import com.yzj.annotion_api.ParameterManager
import com.yzj.annotion_api.RouterManager
import com.yzj.annotion_params.annotations.ARouter
import com.yzj.annotion_params.annotations.Parameter
import com.yzj.common.order.IOrderDrawable


@ARouter(path = "/app/main")
class MainActivity : AppCompatActivity() {


    @Parameter(com.tengfly.arouter.provider.order.DRAWABLE)
    var drawable:IOrderDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ParameterManager.loadParameter(this)

        findViewById<View>(R.id.order).setOnClickListener { gotoOrder(it) }

        findViewById<View>(R.id.personal).setOnClickListener { gotoPersonal(it)  }
        drawable?.let {
            findViewById<ImageView>(R.id.image).setImageResource(it.getLoginDrawableId())
        }


    }

    fun gotoOrder(view: View) {
        RouterManager.build(order.MAIN)
            .withInt("age", 8)
            .navigation(this)
    }

    fun gotoPersonal(view: View) {
        RouterManager.build(personal.MAIN)
            .withString("name", "张无忌")
            .withInt("age", 18)
            .withBoolean("isSex",true)
            .navigation(this)
    }

}