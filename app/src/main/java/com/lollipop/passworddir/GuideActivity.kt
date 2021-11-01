package com.lollipop.passworddir

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewPropertyAnimator
import androidx.core.view.isVisible
import com.lollipop.passworddir.databinding.ActivityGuideBinding
import com.lollipop.passworddir.util.WindowInsetsHelper
import com.lollipop.passworddir.util.fixInsetsByPadding
import com.lollipop.passworddir.util.lazyBind
import com.lollipop.passworddir.util.versionName

class GuideActivity : AppCompatActivity() {

    companion object {
        private const val KEY_GUIDE_VERSION = "GUIDE_VERSION"
    }

    private val binding: ActivityGuideBinding by lazyBind()

    private val guideStepArray = arrayOf(
        GuideStep(R.drawable.guide_step_pwd, R.string.guide_step_pwd),
        GuideStep(R.drawable.guide_step_name, R.string.guide_step_name),
        GuideStep(R.drawable.guide_step_tree, R.string.guide_step_tree),
    )

    private var guideIndex = 0

    private val thisVersion: String by lazy {
        versionName()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (guideStepArray.isEmpty() || !needShowGuide()) {
            toMainPage()
            return
        }
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        binding.root.fixInsetsByPadding(WindowInsetsHelper.Edge.ALL)
        binding.previousBtn.setOnClickListener {
            showLast()
        }
        binding.nextBtn.setOnClickListener {
            showNext()
        }
        showByIndex(0)
    }

    private fun showLast() {
        showByIndex(false)
    }

    private fun showNext() {
        showByIndex(true)
    }

    private fun showByIndex(isNext: Boolean) {
        val pendingOffset = if (isNext) {
            1
        } else {
            -1
        }
        if ((guideIndex + pendingOffset) >= guideStepArray.size) {
            toMainPage()
            return
        }
        binding.imageView.animate()
            .alpha(0F)
            .onceEnd {
                showByIndex(guideIndex + pendingOffset)
                binding.imageView.animate().alpha(1F).start()
            }
            .start()
    }

    private fun showByIndex(index: Int) {
        guideIndex = index
        if (guideIndex < 0) {
            guideIndex = 0
        }
        val guideStep = guideStepArray[guideIndex]
        binding.imageView.setImageResource(guideStep.iconId)
        binding.textView.setText(guideStep.summaryId)
        binding.previousBtn.isVisible = guideIndex > 0
        binding.indexPointView.applyChange {
            pointCount = guideStepArray.size
            selectedIndex = guideIndex
        }
    }

    private fun ViewPropertyAnimator.onceEnd(callback: () -> Unit): ViewPropertyAnimator {
        return setListener(OnceAnimationEndListener {
            setListener(null)
            callback()
        })
    }

    private class OnceAnimationEndListener(
        private val callback: () -> Unit
    ) : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            callback()
            animation?.removeListener(this)
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }

    }

    private data class GuideStep(
        val iconId: Int,
        val summaryId: Int
    )

    private fun toMainPage() {
        saveThisVersion()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun needShowGuide(): Boolean {
        val lastVersion = getPreferences(MODE_PRIVATE).getString(KEY_GUIDE_VERSION, "") ?: ""
        return thisVersion != lastVersion
    }

    private fun saveThisVersion() {
        getPreferences(MODE_PRIVATE).edit().putString(KEY_GUIDE_VERSION, thisVersion).apply()
    }

    override fun onBackPressed() {
        if (guideIndex > 0) {
            showLast()
            return
        }
        super.onBackPressed()
    }

}