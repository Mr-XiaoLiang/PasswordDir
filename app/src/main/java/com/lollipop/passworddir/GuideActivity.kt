package com.lollipop.passworddir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lollipop.passworddir.databinding.ActivityGuideBinding
import com.lollipop.passworddir.util.lazyBind

class GuideActivity : AppCompatActivity() {

    private val binding: ActivityGuideBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}