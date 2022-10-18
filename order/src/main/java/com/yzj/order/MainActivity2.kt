package com.yzj.order

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.tengfly.arouter.order.R
import com.yzj.annotion_api.ParameterManager
import com.yzj.annotion_params.annotations.ARouter
import com.yzj.annotion_params.annotations.Parameter

@ARouter("/order/main2","order1")
class MainActivity2 : AppCompatActivity() {

    @Parameter
    var name:String = "123"

    @Parameter
    var age:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_order)

        ParameterManager.loadParameter(this)

        Log.i("TengFly", "onCreate: name = $name,age = $age")
    }
}