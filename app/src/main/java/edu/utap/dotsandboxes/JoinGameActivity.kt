package edu.utap.dotsandboxes

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_join_game.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_new_game.*
import kotlinx.android.synthetic.main.content_new_game.userDisplayNameET

class JoinGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_game)
        setSupportActionBar(toolbar)
        toolbarText.text = "JOIN GAME"

        setListeners()
    }


    private fun setListeners(){
        startButton.setOnClickListener {
            val joinGameIntent = Intent(this, GameActivity::class.java)
            val extras = Bundle()
            extras.putString(MainActivity.usernameKey, userDisplayNameET.text.toString())
            extras.putString(MainActivity.gameNameKey, joinGameNameET.text.toString())
            extras.putBoolean(MainActivity.joiningGameKey, true)

            startActivity(joinGameIntent)
        }
    }
}