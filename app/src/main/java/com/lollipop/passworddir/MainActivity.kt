package com.lollipop.passworddir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lollipop.passworddir.databinding.ActivityMainBinding
import com.lollipop.passworddir.util.lazyBind

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}