package edu.utap.dotsandboxes

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.dotsandboxes.data.GameData
import edu.utap.dotsandboxes.data.MoveData
import kotlinx.android.synthetic.main.content_game.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class GameActivity : AppCompatActivity() {
    ////////////////////////////
    /// Local Game Variables ///
    ////////////////////////////
    private var gameName = ""
    private var gameWidth = 0
    private var gameHeight = 0
    private var maxPlayerNumber = 0

    private var gameOwner = false
    private val playerColors = arrayListOf(Color.YELLOW, Color.BLUE, Color.GREEN, Color.RED)

    private var columnWidth = 0
    private var rowHeight = 0

    private var columns = 1
    private var rows = 1

    private var circleRadius = 0F

    private var pointMatrix = arrayListOf<ArrayList<Point>>()
    private var squareMatrix = arrayListOf<ArrayList<Square>>()
    private var segments = arrayListOf<Segment>()

    private var startPoint = Point()
    private var endPoint = Point()

    private var playerName = ""
    private var playerNumber = 0

    private var gameData = GameData()
    private var previousMessage : String? = null

    private lateinit var gameBitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint

    private var screenLoaded = false

    //////////////////////////
    /// Firebase Variables ///
    //////////////////////////
    companion object{
        val globalGamesCollection = "GlobalGames"
    }

    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()

    inner class Segment(val a: Point, val b: Point, val player: Int){
        fun equals(p1: Point, p2: Point) : Boolean {
            if (p1 == a && p2 == b) return true
            if (p1 == b && p2 == a) return true
            return false
        }
    }

    inner class Square(val topLeftPoint: Point, val bottomRightPoint: Point, var player: Int?) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setSupportActionBar(null)

        val extras = intent.extras!!
        gameOwner  = !extras.getBoolean(MainActivity.joiningGameKey)
        gameName   = extras.getString(MainActivity.gameNameKey)!!
        playerName = extras.getString(MainActivity.usernameKey)!!

        if(gameOwner){
            maxPlayerNumber = extras.getInt(MainActivity.numberOfPlayersKey)
            columns         = extras.getInt(MainActivity.numberOfColsKey)
            rows            = extras.getInt(MainActivity.numberOfRowsKey)

            establishGameDocument()
        }
        else{
            joinGame()
        }

        setListeners()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus)
        {
            screenLoaded = true
            initializeBoard()
        }
    }

    private fun finishWithMessage(message : String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    ///////////////////////
    /// Initialize Game ///
    ///////////////////////
    private fun setListeners(){
        sendChatButton.setOnClickListener {
            sendChat()
        }

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
                        val segmentCandidate = Segment(startPoint, endPoint, playerNumber)
                        // TODO: Add game logic (A successful add would indicate end of turn)
                        if(!segments.contains(segmentCandidate)) {
                            segments.add(segmentCandidate)
                            performMove()
                        }
                    }
                    startPoint = Point()
                    endPoint = Point()
                }
            }
            true
        }
    }

    private fun performMove(){
        if(!evaluateCompletedSquares()) {
            gameData.turn = (gameData.turn!! + 1) % gameData.currentNumPlayers!!
        }
        segmentsToMoveData()
        updateGame()
    }

    private fun initializeBoard(){

        gameWidth = gameView.width
        gameHeight = gameView.height

        columnWidth = gameWidth / (columns + 1)
        rowHeight = gameHeight / (rows + 1)

        circleRadius = ((columnWidth + rowHeight) / 2F) / 5F

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

    /////////////////////////
    /// Drawing Functions ///
    /////////////////////////
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

        val start = pointMatrix[startPoint.x][startPoint.y]
        val end   = pointMatrix[endPoint.x][endPoint.y]
        canvas.drawLine(start.x.toFloat(), start.y.toFloat(),
                end.x.toFloat(), end.y.toFloat(), paint)
    }

    private fun drawSegments() {
        paint.strokeWidth = circleRadius / 2

        for(segment in segments){
            paint.color = ColorUtils.blendARGB(playerColors[segment.player], Color.BLACK, 0.15f)
            val start = pointMatrix[segment.a.x][segment.a.y]
            val end   = pointMatrix[segment.b.x][segment.b.y]
            canvas.drawLine(start.x.toFloat(), start.y.toFloat(),
                    end.x.toFloat(), end.y.toFloat(), paint)
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
                square.player?.let { player ->
                    paint.color = playerColors[player]
                    canvas.drawRect(
                            square.topLeftPoint.x.toFloat(), square.topLeftPoint.y.toFloat(),
                            square.bottomRightPoint.x.toFloat(), square.bottomRightPoint.y.toFloat(),
                            paint)
                }
            }
        }
    }

    //////////////////
    /// Game Logic ///
    //////////////////

    private fun findAndSetStartPoint(x: Float, y: Float){
        startPoint = findClosestPoint(x,y)
    }

    private fun findAndSetEndPoint(x: Float, y: Float){
        endPoint = findClosestPoint(x,y)
    }

    private fun findClosestPoint(x: Float, y: Float) : Point {
        var closestPoint = Point(gameWidth, gameHeight)
        var currentBestDistance = Float.MAX_VALUE
        for(i in 0 until (columns - 1)){
            for (j in 0 until (rows - 1)){
                val distance = sqrt((x - pointMatrix[i][j].x).pow(2)
                                     + (y - pointMatrix[i][j].y).pow(2))
                if (distance < currentBestDistance) {
                    currentBestDistance = distance
                    closestPoint = Point(i,j)
                }
            }
        }

        return closestPoint
    }

    private fun checkIfPointsAdjacent() : Boolean{
        if(startPoint == endPoint)
            return false
        if(startPoint.x == endPoint.x){
            if(abs(startPoint.y - endPoint.y) == 1) {
                return true
            }
        }
        else if (startPoint.y == endPoint.y){
            if(abs(startPoint.x - endPoint.x) == 1){
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

    private fun evaluateCompletedSquares() : Boolean{
        var newBox = false
        for(i in 0 until (columns - 1)){
            for (j in 0 until (rows - 1)){
                if(segmentsContain(Point(i,j), Point(i,j+1))
                && segmentsContain(Point(i,j), Point(i+1,j))
                && segmentsContain(Point(i,j+1), Point(i+1,j+1))
                && segmentsContain(Point(i+1,j), Point(i+1,j+1))){
                    if(squareMatrix[i][j].player == null){
                        newBox = true
                        squareMatrix[i][j].player = segments.last().player
                    }
                }
            }
        }
        return newBox
    }

    /////////////////////////////////////////
    ////       Firebase Functions        ////
    /////////////////////////////////////////
    private fun updateGame(){
        db.collection(globalGamesCollection)
            .document(gameName)
            .set(gameData)
    }

    private fun moveDataToSegments(){
        segments.clear()
        gameData.moves?.let { moves ->
            for (move in moves) {
                segments.add(
                    Segment(
                        Point(move.firstX!!, move.firstY!!),
                        Point(move.secondX!!, move.secondY!!),
                        move.player!!
                    )
                )
                evaluateCompletedSquares()
            }
        }
    }

    private fun segmentsToMoveData(){
        gameData.moves?.clear()
        for(segment in segments){
            gameData.moves?.add(MoveData(
                segment.a.x,
                segment.a.y,
                segment.b.x,
                segment.b.y,
                segment.player
            ))
        }
    }

    private fun displayMessage(){
        if(previousMessage != gameData.chat){
            previousMessage = gameData.chat
            previousMessage?.let{message ->
                val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.BOTTOM or Gravity.CENTER, 0, -10)
                toast.show()
            }
        }
    }

    private fun watchGame(){
        db.collection(globalGamesCollection).document(gameName)
            .addSnapshotListener { snapshot, error ->
                error?.let {
                    Log.e("Snapshot Error", error.toString())
                    return@addSnapshotListener
                }
                gameData = snapshot!!.toObject(GameData::class.java)!!
                displayMessage()
                moveDataToSegments()
                drawGame()
            }
    }

    private fun joinGame(){
        db.collection(globalGamesCollection).document(gameName).get()
            .addOnSuccessListener {document ->
                document?.let {
                    val data = it.toObject(GameData::class.java)
                    if(data != null){
                        gameData = data
                            if (gameData.started!!) {
                                finishWithMessage("$gameName has already started")
                                return@addOnSuccessListener
                            }
                        if (gameData.finished!!) {
                            finishWithMessage("$gameName has already finished")
                            return@addOnSuccessListener
                        }
                        if (gameData.currentNumPlayers!! >= gameData.maxPlayers!!) {
                            finishWithMessage("$gameName is already full")
                            return@addOnSuccessListener
                        }
                        playerNumber = gameData.currentNumPlayers!!
                        gameData.currentNumPlayers = gameData.currentNumPlayers!! + 1
                        gameData.playerNames?.add(playerName)
                        columns = gameData.numColumns!!
                        rows = gameData.numRows!!

                        updateGame()
                        if (screenLoaded) initializeBoard()
                        watchGame()
                    }
                    else{
                        finishWithMessage("$gameName does not exist.")
                    }
                }
            }
            .addOnFailureListener {
                finishWithMessage("Failed to join $gameName")
            }
    }

    private fun establishGameDocument(){
        gameData = GameData().apply {
            started = false
            finished = false
            maxPlayers = maxPlayerNumber
            currentNumPlayers = 1
            playerNames = arrayListOf(playerName)
            numColumns = columns
            numRows = rows
            moves = arrayListOf()
            turn = 0
        }

        // Make sure game is reset
        db.collection(globalGamesCollection)
            .document(gameName)
            .set(gameData)
            .addOnFailureListener {
                finishWithMessage("Failed to start game")
            }
            .addOnSuccessListener {
                watchGame()
            }
    }

    private fun sendChat(){
        db.collection(globalGamesCollection)
            .document(gameName)
            .update("chat", chatET.text.toString())
            .addOnSuccessListener {
                chatET.text.clear()
                (applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
            }
            .addOnFailureListener {  }
    }
}