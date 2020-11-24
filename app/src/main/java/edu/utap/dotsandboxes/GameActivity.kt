package edu.utap.dotsandboxes

import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content_game.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class GameActivity : AppCompatActivity() {
    private var gameWidth = 0
    private var gameHeight = 0

    private var xSpace = 0
    private var ySpace = 0

    private var x = 10
    private var y = 10

    private var points : ArrayList<Point> = arrayListOf()
    private var segments : ArrayList<Segment> = arrayListOf()

    private var startPoint = Point()
    private var endPoint = Point()

    private lateinit var gameBitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint

    inner class Segment(val a: Point, val b: Point, val color: Int){}

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

            xSpace = gameWidth / (x + 1)
            ySpace = gameHeight / (x + 1)

            initializeBoard()
        }
    }

    private fun setListeners(){
        gameView.setOnTouchListener { v, event ->
            v.performClick()
            when(event.action){
                MotionEvent.ACTION_DOWN ->{
                    findAndSetStartPoint(event.x, event.y)
                }
                MotionEvent.ACTION_MOVE -> {
                    findAndSetEndPoint(event.x, event.y)
                    drawGame()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if(checkIfPointsAdjacent()){
                        val segmentCandidate = Segment(startPoint, endPoint, Color.YELLOW)
                        if(!segments.contains(segmentCandidate)) {
                            segments.add(segmentCandidate)
                        }
                    }
                    startPoint = Point()
                    endPoint = Point()
                    drawGame()
                }
            }
            true
        }
    }

    private fun initializeBoard(){

        paint = Paint()
        paint.isAntiAlias = true

        initializeGrid()
        drawGame()
    }

    private fun initializeGrid(){
        for (i in 1..x) {
            for (j in 1..y) {
                val xCoordinate = i * xSpace
                val yCoordinate = j * ySpace

                points.add(Point(xCoordinate, yCoordinate))
            }
        }
    }

    private fun drawGame(){
        gameBitmap = Bitmap.createBitmap(gameWidth, gameHeight, Bitmap.Config.ARGB_8888)
        canvas = Canvas(gameBitmap)

        drawCurrentLine()
        drawSegments()
        drawCircleGrid()
        gameView.setImageBitmap(gameBitmap)
    }

    private fun drawCurrentLine() {
        paint.color = Color.RED
        paint.strokeWidth = 10F

        canvas.drawLine(startPoint.x.toFloat(), startPoint.y.toFloat(),
                        endPoint.x.toFloat(), endPoint.y.toFloat(), paint)
    }

    private fun drawSegments() {
        paint.strokeWidth = 10F

        for(segment in segments){
            paint.color = segment.color
            canvas.drawLine(segment.a.x.toFloat(), segment.a.y.toFloat(),
                            segment.b.x.toFloat(), segment.b.y.toFloat(), paint)
        }
    }

    private fun drawCircleGrid(){
        paint.color = Color.BLACK
        paint.strokeWidth = 1F

        for(point in points) {
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 20F, paint)
        }
    }

    private fun findAndSetStartPoint(x: Float, y: Float){
        startPoint = findClosestPoint(x,y)
    }

    private fun findAndSetEndPoint(x: Float, y: Float){
        endPoint = findClosestPoint(x,y)
    }

    private fun findClosestPoint(x: Float, y: Float) : Point {
        var closestPoint = Point(gameWidth, gameHeight)
        var currentBestDistance = Float.MAX_VALUE
        for(point in points){
            var distance = sqrt((x - point.x).pow(2) + (y - point.y).pow(2))
            if(distance < currentBestDistance){
                if(distance < 40) {
                    currentBestDistance = distance
                    closestPoint = point
                }
                else{
                    closestPoint = Point(x.toInt(), y.toInt())
                }
            }
        }

        return closestPoint
    }

    private fun checkIfPointsAdjacent() : Boolean{
        if(startPoint.x == endPoint.x){
            if(abs(startPoint.y - endPoint.y) <= ySpace) {
                return true
            }
        }
        else if (startPoint.y == endPoint.y){
            if(abs(startPoint.x - endPoint.x) <= xSpace){
                return true
            }
        }
        return false
    }
}