package com.sega.soni.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sega.soni.R
import com.sega.soni.databinding.VictoryActivityBinding

class VictoryActivity : AppCompatActivity() {

    private lateinit var binding: VictoryActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.victory_activity)

        binding.apply {
            victoryText.text = intent.getStringExtra("text")
            victoryBtn.setOnClickListener {
                with(Intent(this@VictoryActivity, GameActivity::class.java)) {
                    startActivity(this)
                    this@VictoryActivity.finish()
                }
            }
        }
    }

    override fun onBackPressed() {}
}