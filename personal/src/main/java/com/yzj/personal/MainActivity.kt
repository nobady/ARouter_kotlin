package com.yzj.personal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.tengfly.arouter.personal.R
import com.yzj.annotion_api.ParameterManager
import com.yzj.annotion_params.annotations.ARouter
import com.yzj.annotion_params.annotations.Parameter
import com.yzj.common.order.IOrderDrawable

@ARouter("/personal/main")
class MainActivity : AppCompatActivity() {


    @Parameter("/order/drawable")
    var drawable:IOrderDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_personal)
        ParameterManager.loadParameter(this)

        drawable?.let {
            findViewById<ImageView>(R.id.image).setImageResource(it.getLoginDrawableId())
        }
    }
}