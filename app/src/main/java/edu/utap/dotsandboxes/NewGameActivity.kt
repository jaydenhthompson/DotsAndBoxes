package edu.utap.dotsandboxes

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_join_game.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_new_game.*
import kotlinx.android.synthetic.main.content_new_game.userDisplayNameET

class NewGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game)
        setSupportActionBar(toolbar)
        toolbarText.text = "NEW GAME"

        val numPlayersValues = arrayListOf(2,3,4)
        val spinnerAdapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, numPlayersValues)
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        numberOfPlayersSpinner.adapter = spinnerAdapter

        val wValues = (5..10).toList().toTypedArray()
        val widthSpinnerAdapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, wValues)
        widthSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        widthSpinner.adapter = widthSpinnerAdapter

        val hValues = (5..10).toList().toTypedArray()
        val heightSpinnerAdapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, hValues)
        heightSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        heightSpinner.adapter = heightSpinnerAdapter

        setListeners()
    }


    private fun setListeners(){
        startButton.setOnClickListener {
            if(checkValues()){
                val newGameIntent = Intent(this, GameActivity::class.java)
                val extras = Bundle()

                extras.putString(MainActivity.usernameKey, userDisplayNameET.text.toString())
                extras.putString(MainActivity.gameNameKey, newGameNameET.text.toString())
                extras.putBoolean(MainActivity.joiningGameKey, true)

                extras.putInt(MainActivity.numberOfPlayersKey,
                        numberOfPlayersSpinner.selectedItem.toString().toInt())
                extras.putInt(MainActivity.numberOfColsKey,
                        widthSpinner.selectedItem.toString().toInt())
                extras.putInt(MainActivity.numberOfRowsKey,
                        heightSpinner.selectedItem.toString().toInt())

                newGameIntent.putExtras(extras)
                startActivity(newGameIntent)
            }
        }
    }

    private fun checkValues(): Boolean {
        if(newGameNameET.text.isEmpty()){
            Toast.makeText(this,"Game Name is Empty", Toast.LENGTH_SHORT)
                    .show()
            return false
        }

        if(userDisplayNameET.text.isEmpty()){
            Toast.makeText(this,"User Display Name is Empty", Toast.LENGTH_SHORT)
                    .show()
            return false
        }

        return true
    }
}

