package net.android.st069_fakecallphoneprank

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.android.st069_fakecallphoneprank.LanguageAdapter
import net.android.st069_fakecallphoneprank.LanguageItem
import net.android.st069_fakecallphoneprank.databinding.ActivityLanguageBinding
import net.android.st069_fakecallphoneprank.utils.LocaleHelper

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private lateinit var languageList: MutableList<LanguageItem>
    private var selectedLanguageCode: String = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get current language
        val currentLang = LocaleHelper.getLanguage(this)
        selectedLanguageCode = currentLang

        // Language list - exactly 7 languages from image
        languageList = mutableListOf(
            LanguageItem("English", R.drawable.flag_english, false, "en"),
            LanguageItem("Spanish", R.drawable.flag_spanish, false, "es"),
            LanguageItem("French", R.drawable.flag_french, false, "fr"),
            LanguageItem("Hindi", R.drawable.flag_hindi, false, "hi"),
            LanguageItem("Portuguese", R.drawable.flag_portugeese, false, "pt"),
            LanguageItem("German", R.drawable.flag_german, false, "de"),
            LanguageItem("Indonesian", R.drawable.flag_indonesian, false, "id")
        )

        // Mark current language as selected
        languageList.find { it.code == currentLang }?.isSelected = true

        // Setup RecyclerView
        binding.rvLanguages.apply {
            layoutManager = LinearLayoutManager(this@LanguageActivity)
            adapter = LanguageAdapter(languageList) { selected ->
                selectedLanguageCode = selected.code
            }
            addItemDecoration(SpaceItemDecoration(this@LanguageActivity, 16))
        }

        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Done button
        binding.btnDone.setOnClickListener {
            // Save selected language
            LocaleHelper.setLanguage(this, selectedLanguageCode)
            LocaleHelper.setLocale(this)

            // Check if coming from settings
            val fromSettings = intent.getBooleanExtra("from_settings", false)

            if (fromSettings) {
                // From settings - restart app
                setResult(RESULT_OK)
                finish()

                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {
                // From onboarding - continue to intro
                getSharedPreferences("fakecall_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("language_done", true)
                    .apply()

                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            }
        }
    }

    class SpaceItemDecoration(private val context: Context, private val dp: Int) :
        RecyclerView.ItemDecoration() {

        private val space = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = space
        }
    }

    override fun onResume() {
        super.onResume()
        showSystemUI()
    }

    private fun Activity.showSystemUI() {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }
}