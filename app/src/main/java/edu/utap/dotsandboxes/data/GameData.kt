package edu.utap.dotsandboxes.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class GameData(
    var started           : Boolean?           = null,
    var finished          : Boolean?           = null,
    var maxPlayers        : Int?               = null,
    var currentNumPlayers : Int?               = null,
    var playerNames       : ArrayList<String>? = null,
    var numColumns        : Int?               = null,
    var numRows           : Int?               = null,
    var turn              : Int?               = null,

    var moves             : ArrayList<MoveData>? = null,
    var chat              : String ?             = null,

    @ServerTimestamp
    val timestamp : Timestamp? = null,
)
