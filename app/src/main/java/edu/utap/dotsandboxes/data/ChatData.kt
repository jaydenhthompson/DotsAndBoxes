package edu.utap.dotsandboxes.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class ChatData(
        var message: String? = null,
        @ServerTimestamp
        val timeStamp: Timestamp? = null,
)
