package com.eikarna.bluetoothjammer

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import com.eikarna.bluetoothjammer.databinding.ActivityDisclaimerBinding

class DisclaimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDisclaimerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisclaimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUi()
    }

    private fun setupUi() {
        with(binding) {
            githubLink.movementMethod = LinkMovementMethod.getInstance()

            acceptButton.setOnClickListener {
                startMainActivity()
            }

            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}