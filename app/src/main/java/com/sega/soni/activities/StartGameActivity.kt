package com.sega.soni.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sega.soni.R
import com.sega.soni.databinding.StartGameActivityBinding

class StartGameActivity : AppCompatActivity() {

    private lateinit var binding: StartGameActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.start_game_activity)

        binding.startBtn.setOnClickListener {
            with(Intent(this, GameActivity::class.java)) {
                startActivity(this)
                this@StartGameActivity.finish()
            }
        }
    }

    override fun onBackPressed() {}
}