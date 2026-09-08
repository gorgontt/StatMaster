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
        val content = "Заголовок\nПараграф 1\nПараграф 2\nПараграф 3"
        val result = parseDocumentContent(content, null)

        val paragraphs = result.content.filterIsInstance<ContentBlock.Paragraph>()
        assertEquals("Should have 3 paragraphs", 3, paragraphs.size)
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
    fun `parseDocumentContent handles subtitles`() {
        val content = "Заголовок\n-- Подзаголовок\nТекст под подзаголовком"
        val result = parseDocumentContent(content, null)

        val subtitles = result.content.filterIsInstance<ContentBlock.Paragraph>()
        assertTrue("Should have subtitle", subtitles.any { it.text == "Подзаголовок" })
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

    @Test
    fun `parseDocumentContent handles empty content`() {
        val content = ""
        val result = parseDocumentContent(content, null)

        assertEquals("Title should be empty", "", result.title)
        assertTrue("Content should be empty", result.content.isEmpty())
    }

    @Test
    fun `parseDocumentContent replaces special characters`() {
        val content = "Заголовок\nТекст с  и  и "
        val result = parseDocumentContent(content, null)

        val paragraphs = result.content.filterIsInstance<ContentBlock.Paragraph>()
        assertTrue("Should replace  with ⊂", paragraphs[0].text.contains("⊂"))
        assertTrue("Should replace  with Ω", paragraphs[0].text.contains("Ω"))
        assertTrue("Should replace  with ∅", paragraphs[0].text.contains("∅"))
    }
}