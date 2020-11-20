package edu.utap.dotsandboxes

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_new_game.*
import kotlinx.android.synthetic.main.content_statistics.*

class StatisticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        setSupportActionBar(toolbar)
        toolbarText.text = "STATISTICS"

        setListeners()
    }


    private fun setListeners(){
        backButton.setOnClickListener {
            finish()
        }
    }
}