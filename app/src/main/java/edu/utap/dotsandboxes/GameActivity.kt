package edu.utap.dotsandboxes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content_game.*

class GameActivity : AppCompatActivity() {
    private var gameWidth = 0
    private var gameHeight = 0

    private lateinit var gameBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setSupportActionBar(null)

        setListeners()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus)
        {
            gameWidth = gameView.width
            gameHeight = gameView.measuredHeight

            drawCircleGrid(10,10)
        }
    }

    private fun setListeners(){
        gameView.setOnTouchListener { v, event ->
            v.performClick()
            event.
            true
        }

    }

    private fun drawCircleGrid(x: Int, y: Int){
        val width = gameView.measuredWidth
        val height = gameView.measuredHeight

        val paint = Paint()
        val gameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(gameBitmap)
        paint.color = Color.BLACK
        paint.isAntiAlias = true

        for (i in 1..x){
            for (j in 1..y){
                val xCoordinate = i * width / (x + 1)
                val yCoordinate = j * height / (y + 1)

                canvas.drawCircle(xCoordinate.toFloat(), yCoordinate.toFloat(), 20F, paint)
            }
        }
        gameView.setImageBitmap(gameBitmap)
    }
}