package edu.utap.dotsandboxes

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
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

        val numPlayersValues = arrayListOf(2,3,4)
        val spinnerAdapter = ArrayAdapter<Int>(
            this, android.R.layout.simple_spinner_item, numPlayersValues)
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        numberOfPlayersSpinner.adapter = spinnerAdapter

        val wValues = (5..10).toList().toTypedArray()
        val widthSpinnerAdapter = ArrayAdapter<Int>(
            this, android.R.layout.simple_spinner_item, wValues)
        widthSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        widthSpinner.adapter = widthSpinnerAdapter

        val hValues = (5..10).toList().toTypedArray()
        val heightSpinnerAdapter = ArrayAdapter<Int>(
            this, android.R.layout.simple_spinner_item, hValues)
        heightSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        heightSpinner.adapter = heightSpinnerAdapter

        setListeners()
    }


    private fun setListeners(){
        startButton.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
    }
}