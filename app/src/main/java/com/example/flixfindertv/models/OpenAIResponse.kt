package com.example.flixfindertv.models

data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
