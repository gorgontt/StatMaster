package com.example.statmaster

import com.example.statmaster.terver.parseDocumentContent
import org.junit.Assert.*
import org.junit.Test

class DocumentParserTest {

    @Test
    fun `parseDocumentContent extracts title correctly`() {
        val content = "Заголовок\nПараграф 1\nПараграф 2"
        val result = parseDocumentContent(content, null)

        assertEquals("Заголовок", result.title)
    }

    @Test
    fun `parseDocumentContent creates paragraphs`() {
        val content = "Заголовок\nПараграф 1\nПараграф 2"
        val result = parseDocumentContent(content, null)

        val paragraphs = result.content.filterIsInstance<ContentBlock.Paragraph>()
        assertEquals("Should have 2 paragraphs", 2, paragraphs.size)
    }

    @Test
    fun `parseDocumentContent handles quotes`() {
        val content = "Заголовок\n> Цитата 1\n> Цитата 2\nОбычный текст"
        val result = parseDocumentContent(content, null)

        val quotes = result.content.filterIsInstance<ContentBlock.Quote>()
        assertTrue("Should have a quote", quotes.isNotEmpty())
        assertTrue("Quote should contain both lines", quotes[0].text.contains("Цитата 1"))
        assertTrue("Quote should contain both lines", quotes[0].text.contains("Цитата 2"))
    }

    @Test
    fun `parseDocumentContent handles image URL`() {
        val content = "Заголовок\nТекст"
        val imageUrl = "https://example.com/image.png"
        val result = parseDocumentContent(content, imageUrl)

        val images = result.content.filterIsInstance<ContentBlock.Image>()
        assertEquals("Should have one image", 1, images.size)
        assertEquals("Image URL should match", imageUrl, images[0].url)
    }
}