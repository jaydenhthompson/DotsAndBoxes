package edu.utap.dotsandboxes.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class MoveData(
    var firstX  : Int? = null,
    var firstY  : Int? = null,
    var secondX : Int? = null,
    var secondY : Int? = null,
    var color   : Int? = null,

    @ServerTimestamp
    val timestamp : Timestamp? = null,
    val rowID: String = ""
)
