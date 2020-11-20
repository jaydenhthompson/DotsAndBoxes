package edu.utap.dotsandboxes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbarText.text = "DOTS & BOXES"

        setListeners()
    }

    private fun setListeners(){
        newGameButton.setOnClickListener {
            startActivity(Intent(this, NewGameActivity::class.java))
        }

        joinGameButton.setOnClickListener {
            startActivity(Intent(this, JoinGameActivity::class.java))
        }

        statisticsButton.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
    }
}