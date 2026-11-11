package com.example.data.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri

/** Creates a compact, paginated PDF using the Android platform renderer. */
object PdfNoteExporter {
    fun write(context: Context, uri: Uri, title: String, content: String, tags: List<String>) {
        val output = context.contentResolver.openOutputStream(uri)
            ?: error("Could not open destination for writing")
        val document = PdfDocument()
        try {
            val pageWidth = 595
            val pageHeight = 842
            val margin = 52f
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(22, 21, 26)
                textSize = 25f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(70, 68, 76)
                textSize = 12f
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(108, 104, 117)
                textSize = 10f
            }
            val maxWidth = pageWidth - margin * 2
            val lines = buildList {
                content.lines().forEach { paragraph ->
                    if (paragraph.isBlank()) {
                        add("")
                    } else {
                        var line = ""
                        paragraph.split(Regex("\\s+")).forEach { word ->
                            val candidate = if (line.isEmpty()) word else "$line $word"
                            if (bodyPaint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                                add(line)
                                line = word
                            } else line = candidate
                        }
                        if (line.isNotEmpty()) add(line)
                    }
                }
                if (tags.isNotEmpty()) {
                    add("")
                    add("Calaamadaha: " + tags.joinToString("  ") { "#$it" })
                }
            }
            var pageNumber = 1
            var lineIndex = 0
            do {
                val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                val canvas = page.canvas
                canvas.drawColor(android.graphics.Color.WHITE)
                var y = margin + 12f
                if (pageNumber == 1) {
                    canvas.drawText(title.ifBlank { "Xasuus" }, margin, y, titlePaint)
                    y += 34f
                    canvas.drawLine(margin, y, pageWidth - margin, y, mutedPaint)
                    y += 24f
                }
                val lineHeight = 19f
                while (lineIndex < lines.size && y < pageHeight - margin) {
                    val line = lines[lineIndex]
                    canvas.drawText(line, margin, y, if (line.startsWith("Calaamadaha:")) mutedPaint else bodyPaint)
                    y += lineHeight
                    lineIndex++
                }
                canvas.drawText("Xasuus · bogga $pageNumber", margin, pageHeight - 24f, mutedPaint)
                document.finishPage(page)
                pageNumber++
            } while (lineIndex < lines.size)
            if (lines.isEmpty()) {
                val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                page.canvas.drawColor(android.graphics.Color.WHITE)
                page.canvas.drawText(title.ifBlank { "Xasuus" }, margin, margin + 12f, titlePaint)
                document.finishPage(page)
            }
            document.writeTo(output)
        } finally {
            output.close()
            document.close()
        }
    }
}
