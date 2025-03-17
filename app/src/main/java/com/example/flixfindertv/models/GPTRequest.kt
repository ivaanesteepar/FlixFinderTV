package com.example.flixfindertv.models

data class GPTRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)
