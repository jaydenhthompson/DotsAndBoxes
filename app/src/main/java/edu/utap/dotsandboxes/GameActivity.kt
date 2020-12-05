package edu.utap.dotsandboxes

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.dotsandboxes.data.GameData
import edu.utap.dotsandboxes.data.MoveData
import kotlinx.android.synthetic.main.content_game.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class GameActivity : AppCompatActivity() {
    ////////////////////////////
    /// Local Game Variables ///
    ////////////////////////////
    private var gameUuid = ""
    private var gameName = ""
    private var gameWidth = 0
    private var gameHeight = 0
    private var maxPlayerNumber = 0
    private var done = false

    private var gameOwner = false
    private val playerColors = arrayListOf(Color.YELLOW, Color.BLUE, Color.GREEN, Color.RED)
    private val scores = arrayListOf(0,0,0,0)

    private var columnWidth = 0
    private var rowHeight = 0

    private var columns = 0
    private var rows = 0

    private var circleRadius = 0F

    private var pointMatrix = arrayListOf<ArrayList<Point>>()
    private var squareMatrix = arrayListOf<ArrayList<Square>>()
    private var segments = arrayListOf<Segment>()

    private var startPoint = Point()
    private var endPoint = Point()

    private var playerName = ""
    private var playerNumber = 0

    private var gameData = GameData()

    private lateinit var gameBitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private lateinit var toast : Toast

    private var screenLoaded = false

    //////////////////////////
    /// Firebase Variables ///
    //////////////////////////
    companion object{
        const val globalGamesCollection = "GlobalGames"
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

            gameUuid = UUID.randomUUID().toString()

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
            if(columns != 0) {
                initializeBoard()
            }
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
        gameView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if(screenLoaded){
                if(columns != 0) {
                    initializeGrid()
                    drawGame()
                }
            }
        }
        sendChatButton.setOnClickListener {
            sendChat()
        }

        gameView.setOnTouchListener { v, event ->
            v.performClick()
            var message = ""

            if(!gameData.finished!!) {
                if (gameData.turn != playerNumber) {
                    event.action = MotionEvent.ACTION_CANCEL
                    message = "It's not your turn"
                }
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        findAndSetStartPoint(event.x, event.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        findAndSetEndPoint(event.x, event.y)
                        drawGame()
                    }
                    MotionEvent.ACTION_UP -> {
                        if (gameData.currentNumPlayers!! <= 1) {
                            displayMessage("Trying to play by yourself?")
                            drawGame()
                        } else if (checkIfPointsAdjacent()) {
                            val segmentCandidate = Segment(startPoint, endPoint, playerNumber)
                            if (!segmentsContain(segmentCandidate.a, segmentCandidate.b)) {
                                segments.add(segmentCandidate)
                                performMove()
                            }
                        }
                        startPoint = Point()
                        endPoint = Point()
                        drawGame()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        displayMessage(message)
                    }
                }
            }
            true
        }
    }

    private fun performMove(){
        if(gameOwner) gameData.started = true
        if(!evaluateCompletedSquares()) {
            gameData.turn = (gameData.turn!! + 1) % gameData.currentNumPlayers!!
        }
        if(done){
            gameData.finished = true
        }
        segmentsToMoveData()
        updateGame()
    }

    @SuppressLint("ShowToast")
    private fun initializeBoard(){
        paint = Paint()
        paint.isAntiAlias = true
        toast = Toast.makeText(this, "", Toast.LENGTH_LONG)

        chatTV.movementMethod = ScrollingMovementMethod()

        initPointMatrix()
        initializeSquareMatrix()
        initializeGrid()
        drawGame()
    }

    private fun initPointMatrix(){
        for (i in 0 until columns) {
            pointMatrix.add(arrayListOf())
            for (j in 0 until rows) {
                pointMatrix[i].add(Point())
            }
        }
    }

    private fun initializeGrid(){
        gameWidth = gameView.width
        gameHeight = gameView.height

        columnWidth = gameWidth / columns
        rowHeight = gameHeight / rows

        circleRadius = ((columnWidth + rowHeight) / 2F) / 5F

        for (i in 0 until columns) {
            for (j in 0 until rows) {
                val xCoordinate = (i * columnWidth) + (columnWidth / 2)
                val yCoordinate = (j * rowHeight) + (rowHeight / 2)

                pointMatrix[i][j] = Point(xCoordinate, yCoordinate)
            }
        }
    }

    private fun initializeSquareMatrix() {
        for(i in 0 until (columns - 1)){
            squareMatrix.add(arrayListOf())
            for (j in 0 until (rows - 1)){
                squareMatrix[i].add(Square(Point(i,j), Point(i+1,j+1), null))
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
        setChatText()
        drawScores()
        drawTurn()
    }

    private fun drawCurrentLine() {
        paint.color = Color.BLACK
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
                    val topLeft = pointMatrix[square.topLeftPoint.x][square.topLeftPoint.y]
                    val bottomRight = pointMatrix[square.bottomRightPoint.x][square.bottomRightPoint.y]
                    canvas.drawRect(
                            topLeft.x.toFloat(), topLeft.y.toFloat(),
                            bottomRight.x.toFloat(), bottomRight.y.toFloat(),
                            paint)
                }
            }
        }
    }

    private fun drawScores(){
        yellowScoreTV.text = scores[playerColors.indexOf(Color.YELLOW)].toString()
        blueScoreTV.text = scores[playerColors.indexOf(Color.BLUE)].toString()
        greenScoreTV.text = scores[playerColors.indexOf(Color.GREEN)].toString()
        redScoreTV.text = scores[playerColors.indexOf(Color.RED)].toString()
    }

    private fun setChatText(){
        chatTV.text = gameData.chat
    }

    private fun hideKeyboard(){
        (applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
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
        for(i in 0 until (columns)){
            for (j in 0 until (rows)){
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
        var allSquaresFilled = true
        for(i in 0 until (columns - 1)){
            for (j in 0 until (rows - 1)){
                if(segmentsContain(Point(i,j), Point(i,j+1))
                && segmentsContain(Point(i,j), Point(i+1,j))
                && segmentsContain(Point(i,j+1), Point(i+1,j+1))
                && segmentsContain(Point(i+1,j), Point(i+1,j+1))){
                    if(squareMatrix[i][j].player == null){
                        newBox = true
                        val segmentPlayer = segments.last().player
                        squareMatrix[i][j].player = segmentPlayer
                        scores[segmentPlayer]++
                    }
                }
               if(squareMatrix[i][j].player == null)
                   allSquaresFilled = false
            }
        }
        if(allSquaresFilled) done = true
        return newBox
    }

    private fun drawTurn() {
        yellowScoreTV.setBackgroundColor(Color.WHITE)
        blueScoreTV.setBackgroundColor(Color.WHITE)
        greenScoreTV.setBackgroundColor(Color.WHITE)
        redScoreTV.setBackgroundColor(Color.WHITE)

        when(gameData.turn) {
            0 -> yellowScoreTV.setBackgroundColor(Color.GRAY)
            1 -> blueScoreTV . setBackgroundColor (Color.GRAY)
            2 -> greenScoreTV . setBackgroundColor (Color.GRAY)
            3 -> redScoreTV . setBackgroundColor (Color.GRAY)
        }
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
        Log.d("segments", segments.toString())
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

    private fun displayMessage(message: String){
        toast.cancel()
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun watchGame(){
        sendChat(true)
        db.collection(globalGamesCollection).document(gameName)
            .addSnapshotListener { snapshot, error ->
                error?.let {
                    Log.e("Snapshot Error", error.toString())
                    return@addSnapshotListener
                }
                gameData = snapshot!!.toObject(GameData::class.java)!!
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
                        if (gameData.currentNumPlayers!! >= gameData.maxPlayers!!) {
                            finishWithMessage("$gameName is already full")
                            return@addOnSuccessListener
                        }
                        gameUuid = gameData.uuid!!
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
            uuid = gameUuid
            started = false
            finished = false
            maxPlayers = maxPlayerNumber
            currentNumPlayers = 1
            playerNames = arrayListOf(playerName)
            numColumns = columns
            numRows = rows
            moves = arrayListOf()
            turn = 0
            chat = ""
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

    private fun sendChat(joinMessage : Boolean = false){
        var addition = chatET.text.toString()
        if(joinMessage) addition = "JOINED"

        val chatText = "${gameData.chat!!}\n$playerName: $addition"
        db.collection(globalGamesCollection)
            .document(gameName)
            .update("chat", chatText)
            .addOnSuccessListener {
                chatET.text.clear()
                hideKeyboard()
            }
            .addOnFailureListener {  }
    }
}