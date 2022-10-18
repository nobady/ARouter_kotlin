package com.yzj.personal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tengfly.arouter.personal.R
import com.yzj.annotion_params.annotations.ARouter

@ARouter("/personal/main","personal")
class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_personal)
    }
}