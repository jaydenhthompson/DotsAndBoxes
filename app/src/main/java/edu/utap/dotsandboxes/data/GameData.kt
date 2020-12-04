package edu.utap.dotsandboxes.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class GameData(
    var started           : Boolean? = null,
    var finished          : Boolean? = null,
    var maxPlayers        : Int?     = null,
    var currentNumPlayers : Int?     = null,
    var columns           : Int?     = null,
    var rows              : Int?     = null,
    var turn              : Int?     = null,

    @ServerTimestamp
    val timestamp : Timestamp? = null,
    val rowID: String = ""
)
