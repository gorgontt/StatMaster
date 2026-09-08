package com.example.statmaster.terver

import com.example.statmaster.ContentBlock
import com.example.statmaster.ParsedDocument
import com.example.statmaster.QuestionWithAnswers

fun parseDocumentContent(content: String, imageUrl: String?): ParsedDocument {
    val normalizedContent = content
        .replace("", "⊂")
        .replace("", "Ω")
        .replace("", "∅")

    val lines = normalizedContent.lines()
    if (lines.isEmpty()) return ParsedDocument("", listOf())

    val title = lines.first().trim()
    val contentBlocks = mutableListOf<ContentBlock>()
    var currentQuote: StringBuilder? = null

    for (line in lines.drop(1)) {
        val trimmedLine = line.trim()
        if (trimmedLine.isEmpty()) continue

        when {
            trimmedLine.startsWith("--") -> {
                currentQuote?.let {
                    contentBlocks.add(ContentBlock.Quote(it.toString()))
                    currentQuote = null
                }
                contentBlocks.add(ContentBlock.Paragraph(trimmedLine.substring(2).trim()))
            }
            trimmedLine.startsWith(">") -> {
                if (currentQuote == null) {
                    currentQuote = StringBuilder(trimmedLine.substring(1).trim())
                } else {
                    currentQuote!!.append("\n").append(trimmedLine.substring(1).trim())
                }
            }
            else -> {
                currentQuote?.let {
                    contentBlocks.add(ContentBlock.Quote(it.toString()))
                    currentQuote = null
                }
                contentBlocks.add(ContentBlock.Paragraph(trimmedLine))
            }
        }
    }

    currentQuote?.let {
        contentBlocks.add(ContentBlock.Quote(it.toString()))
    }

    imageUrl?.let {
        contentBlocks.add(ContentBlock.Image(it))
    }

    return ParsedDocument(title, contentBlocks)
}

fun calculateScore(questions: List<QuestionWithAnswers>, userAnswers: Map<Int, Int?>): Pair<Int, Int> {
    var correct = 0
    questions.forEach { question ->
        val selectedAnswerId = userAnswers[question.id]
        if (selectedAnswerId != null) {
            val selectedAnswer = question.answers.firstOrNull { it.id == selectedAnswerId }
            if (selectedAnswer?.isCorrect == true) {
                correct++
            }
        }
    }
    return Pair(correct, questions.size)
}