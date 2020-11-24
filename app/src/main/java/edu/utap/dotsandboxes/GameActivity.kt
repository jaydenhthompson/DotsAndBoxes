package edu.utap.dotsandboxes

import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content_game.*

class GameActivity : AppCompatActivity() {
    private var gameWidth = 0
    private var gameHeight = 0
    private var x = 10
    private var y = 10

    private var points : ArrayList<Pair<Int, Int>> = arrayListOf()

    private val originPoint = Point()

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
            gameHeight = gameView.height

            drawCircleGrid(x,y)
        }
    }

    private fun setListeners(){
        gameView.setOnTouchListener { v, event ->
            v.performClick()
            when(event.action){
                MotionEvent.ACTION_DOWN ->{
                    //originPoint = Point(event.x, event.y)
                   Toast.makeText(this, "${event.x} $gameWidth", Toast.LENGTH_SHORT).show()
                }
                MotionEvent.ACTION_MOVE -> {
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                }
            }
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
                points.add(Pair(xCoordinate, yCoordinate))

                canvas.drawCircle(xCoordinate.toFloat(), yCoordinate.toFloat(), 20F, paint)
            }
        }
        gameView.setImageBitmap(gameBitmap)
    }
}