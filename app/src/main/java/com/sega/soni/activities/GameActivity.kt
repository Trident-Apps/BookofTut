package com.sega.soni.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.sega.soni.R
import com.sega.soni.databinding.GameActivityBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {

    private lateinit var binding: GameActivityBinding
    private var tries = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.game_activity)

        listOf(
            binding.imageView,
            binding.imageView2,
            binding.imageView3,
            binding.imageView4,
            binding.imageView5,
            binding.imageView6
        ).forEach { imageView ->
            imageView.setOnClickListener {
                play(it as ImageView)
            }
        }
    }

    private fun play(view: ImageView) {
        view.animate().apply {
            duration = 1000L
            rotationYBy(360f)
        }.withEndAction {
            val list = listOf(1, 2, 3).shuffled()
            if (list[0] == 1) {
                lifecycleScope.launch {
                    view.setImageResource(R.drawable.ic)
                    delay(1000)
                    startAfterMatch(YOU_WON)
                }
            } else {
                val ivList = listOf(R.drawable.ic3, R.drawable.ic4).shuffled()
                view.setImageResource(ivList[0])
                view.isClickable = false
                tries--
                binding.triesTv.text = "You have $tries tries"
            }
            if (tries == 0) {
                lifecycleScope.launch {
                    delay(1000)
                    startAfterMatch(YOU_LOOSE)
                }
            }
        }
    }

    private fun startAfterMatch(text: String) {
        with(Intent(this, VictoryActivity::class.java)) {
            this.putExtra("text", text)
            startActivity(this)
            this@GameActivity.finish()
        }
    }


    override fun onBackPressed() {}

    companion object {
        const val YOU_WON = "You won, try again?"
        const val YOU_LOOSE = "You loose, try again?"
    }
}