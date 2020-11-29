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

    private var columnWidth = 0
    private var rowHeight = 0

    private var columns = 6
    private var rows = 10

    private var circleRadius = 0F

    private var pointMatrix = arrayListOf<ArrayList<Point>>()
    private var squareMatrix = arrayListOf<ArrayList<Square>>()
    private var segments = arrayListOf<Segment>()

    private var startPoint = Point()
    private var endPoint = Point()
    private var playerColor = Color.YELLOW

    private lateinit var gameBitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint

    inner class Segment(val a: Point, val b: Point, val color: Int){
        fun equals(p1: Point, p2: Point) : Boolean {
            if (p1 == a && p2 == b) return true
            if (p1 == b && p2 == a) return true
            return false
        }
    }

    inner class Square(val topLeftPoint: Point, val bottomRightPoint: Point, val color: Int?) {
    }

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

            columnWidth = gameWidth / (columns + 1)
            rowHeight = gameHeight / (rows + 1)

            circleRadius = ((columnWidth + rowHeight) / 2F) / 5F

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
                        val segmentCandidate = Segment(startPoint, endPoint, playerColor)
                        // TODO: Add game logic (A successful add would indicate end of turn)
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
        for (i in 1..columns) {
            pointMatrix.add(arrayListOf())
            for (j in 1..rows) {
                val xCoordinate = i * columnWidth
                val yCoordinate = j * rowHeight

                pointMatrix[i - 1].add(Point(xCoordinate, yCoordinate))
            }
        }
        for(i in 0 until (columns - 1)){
            squareMatrix.add(arrayListOf())
            for (j in 0 until (rows - 1)){
                squareMatrix[i].add(Square(pointMatrix[i][j], pointMatrix[i+1][j+1], null))
            }
        }
    }

    private fun drawGame(){
        gameBitmap = Bitmap.createBitmap(gameWidth, gameHeight, Bitmap.Config.ARGB_8888)
        canvas = Canvas(gameBitmap)

        drawCurrentLine()
        drawSquares()
        drawSegments()
        drawCircleGrid()
        gameView.setImageBitmap(gameBitmap)
    }

    private fun drawCurrentLine() {
        paint.color = Color.RED
        paint.strokeWidth = circleRadius / 2

        canvas.drawLine(startPoint.x.toFloat(), startPoint.y.toFloat(),
                        endPoint.x.toFloat(), endPoint.y.toFloat(), paint)
    }

    private fun drawSegments() {
        paint.strokeWidth = circleRadius / 2

        for(segment in segments){
            paint.color = segment.color
            canvas.drawLine(segment.a.x.toFloat(), segment.a.y.toFloat(),
                            segment.b.x.toFloat(), segment.b.y.toFloat(), paint)
        }
    }

    private fun drawCircleGrid(){
        paint.color = Color.BLACK
        paint.strokeWidth = 1F

        for(column in pointMatrix) {
            for(point in column) {
                canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), circleRadius, paint)
            }
        }
    }

    private fun drawSquares(){
        for(column in squareMatrix){
            for(square in column) {
                square.color?.let { color ->
                    paint.color = color
                    canvas.drawRect(
                            square.topLeftPoint.x.toFloat(), square.topLeftPoint.y.toFloat(),
                            square.bottomRightPoint.x.toFloat(), square.bottomRightPoint.y.toFloat(),
                            paint)
                }
            }
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
        for(column in pointMatrix){
            for(point in column) {
                val distance = sqrt((x - point.x).pow(2) + (y - point.y).pow(2))
                if (distance < currentBestDistance) {
                    if (distance < circleRadius * 2) {
                        currentBestDistance = distance
                        closestPoint = point
                    } else {
                        closestPoint = Point(x.toInt(), y.toInt())
                    }
                }
            }
        }

        return closestPoint
    }

    private fun checkIfPointsAdjacent() : Boolean{
        if(startPoint.x == endPoint.x){
            if(abs(startPoint.y - endPoint.y) <= rowHeight) {
                return true
            }
        }
        else if (startPoint.y == endPoint.y){
            if(abs(startPoint.x - endPoint.x) <= columnWidth){
                return true
            }
        }
        return false
    }

    private fun segmentsContain(a : Point, b : Point) : Boolean {
        for (segment in segments){
            if(segment.equals(a, b)) return true
        }
        return false
    }

    private fun evaluateCompletedSquares(){
        for(i in 0 until (columns - 1)){
            for (j in 0 until (rows - 1)){
                
            }
        }
    }
}