package edu.utap.dotsandboxes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val usernameKey        = "usernameKey"
        const val joiningGameKey     = "joiningGameKey"
        const val gameNameKey        = "gameNameKey"
        const val numberOfPlayersKey = "numberOfPlayersKey"
        const val numberOfRowsKey    = "numberOfRowsKey"
        const val numberOfColsKey    = "numberOfColsKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbarText.text = "DOTS & BOXES"

        val provider = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(provider)
                .build(),
            1
        )

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