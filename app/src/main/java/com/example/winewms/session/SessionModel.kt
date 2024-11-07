package com.example.winewms.session

data class SessionModel(
    val sessionId: Int,
    val sessionStart: String,
    val sessionEnd: String,
    val sessionStatus: Int,
    val accountId: Int
)