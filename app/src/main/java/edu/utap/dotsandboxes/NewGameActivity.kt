package edu.utap.dotsandboxes

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_new_game.*

class NewGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game)
        setSupportActionBar(toolbar)
        toolbarText.text = "NEW GAME"

        numberOfPlayersPicker.minValue = 2
        numberOfPlayersPicker.maxValue = 4
        numberOfPlayersPicker.value = 3
    }


    private fun setListeners(){
    }
}