package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.Customer
import com.example.data.Measurement
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    fun generateMeasurementPdf(
        context: Context,
        shopName: String,
        shopPhone: String,
        shopAddress: String,
        customer: Customer,
        measurements: List<Measurement>,
        language: String
    ): File? {
        val pdfDocument = PdfDocument()
        val pageBuilder = PageBuilder(pdfDocument, shopName, customer.name)
        
        var canvas = pageBuilder.startNewPage()
        
        // Setup Paints with increased font sizes for better readability
        val brandPaint = Paint().apply {
            color = Color.rgb(220, 38, 38) // red-600
            textSize = 12f // increased from 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // slate-900
            textSize = 24f // increased from 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.rgb(71, 85, 105) // slate-600
            textSize = 12f // increased from 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.rgb(100, 116, 139) // slate-500
            textSize = 11f // increased from 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val boldValuePaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // slate-900
            textSize = 11f // increased from 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyTextPaint = Paint().apply {
            color = Color.rgb(51, 65, 85) // slate-700
            textSize = 11f // increased from 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240) // slate-200
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        var y = 40f

        // 1. Header (Shop Name)
        val nameToDraw = if (shopName.isBlank()) "MeasureBook" else shopName
        canvas.drawText(nameToDraw, 40f, y + 16f, titlePaint)
        
        val appSubtitle = if (language == "gu") "માપપોથી (Measurement Book)" else "MEASUREMENT BOOK"
        canvas.drawText(appSubtitle.uppercase(), 40f, y + 31f, brandPaint)
        
        y += 45f
        if (shopPhone.isNotBlank() || shopAddress.isNotBlank()) {
            val contactParts = mutableListOf<String>()
            if (shopPhone.isNotBlank()) contactParts.add("Ph: $shopPhone")
            if (shopAddress.isNotBlank()) contactParts.add(shopAddress)
            val contactStr = contactParts.joinToString(" | ")
            canvas.drawText(contactStr, 40f, y, subtitlePaint)
            y += 14f
        }
        
        // Header bottom divider line
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Localized Strings for details
        val labelCustomerDetails = if (language == "gu") "ગ્રાહકની વિગતો" else "CUSTOMER DETAILS"
        val labelMeasurementHistory = if (language == "gu") "માપનો ઇતિહાસ" else "MEASUREMENT HISTORY"
        val labelNoMeasurements = Localization.get("no_measurements", language)
        val labelNotes = Localization.get("notes", language)

        // 2. Customer Details Box Section Header
        val headerBarPaint = Paint().apply {
            color = Color.rgb(220, 38, 38) // red-600
            style = Paint.Style.FILL
        }
        canvas.drawRect(40f, y, 44f, y + 14f, headerBarPaint)
        
        val sectionTitlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // slate-900
            textSize = 12.5f // increased from 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(labelCustomerDetails.uppercase(), 49f, y + 11f, sectionTitlePaint)
        y += 22f
        
        val boxTop = y
        
        // First measure to get bottom coordinate
        val boxBottom = measureAndDrawCustomerBox(
            canvas = null,
            startY = boxTop,
            customer = customer,
            language = language,
            labelPaint = labelPaint,
            bodyTextPaint = bodyTextPaint,
            boldValuePaint = boldValuePaint
        )
        
        // Draw the background card
        val boxBgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252) // slate-50
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val boxBorderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240) // slate-200
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val boxRect = android.graphics.RectF(40f, boxTop, 555f, boxBottom)
        canvas.drawRoundRect(boxRect, 8f, 8f, boxBgPaint)
        canvas.drawRoundRect(boxRect, 8f, 8f, boxBorderPaint)
        
        // Draw the text inside the box
        measureAndDrawCustomerBox(
            canvas = canvas,
            startY = boxTop,
            customer = customer,
            language = language,
            labelPaint = labelPaint,
            bodyTextPaint = bodyTextPaint,
            boldValuePaint = boldValuePaint
        )
        
        y = boxBottom + 25f

        // 3. Measurement History Section Header
        canvas.drawRect(40f, y, 44f, y + 14f, headerBarPaint)
        canvas.drawText(labelMeasurementHistory.uppercase(), 49f, y + 11f, sectionTitlePaint)
        y += 24f

        if (measurements.isEmpty()) {
            canvas.drawText(labelNoMeasurements, 40f, y, bodyTextPaint)
        } else {
            for (measurement in measurements) {
                // Compute height needed dynamically
                val isBlouse = measurement.category == "blouse"
                val keys = measurement.fields.keys.toList()
                val gridRows = Math.ceil(keys.size / 2.0).toInt() // changed to 2 columns to prevent overlap
                val hasNotes = !measurement.notes.isNullOrBlank()
                
                val mNotesLabel = "$labelNotes: "
                val mNotesPaint = Paint().apply {
                    color = Color.rgb(71, 85, 105) // slate-600
                    textSize = 11f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }
                val labelPaintM = Paint().apply {
                    color = Color.rgb(100, 116, 139) // slate-500
                    textSize = 11f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                val notesLabelW = labelPaintM.measureText(mNotesLabel)
                val notesLines = if (hasNotes) wrapText(measurement.notes ?: "", mNotesPaint, 515f - notesLabelW) else emptyList()
                
                // Block components: header line + spacer + cells height + notes + spacing
                val estimatedHeight = 20f + 4f + (gridRows * 30f) + (if (hasNotes) (notesLines.size * 14.3f) + 8f else 0f) + 20f
                
                // Pagination check
                if (y + estimatedHeight > 780f) {
                    canvas = pageBuilder.startNewPage()
                    y = 65f
                }
                
                // Draw Measurement Title & Date
                val catColor = if (isBlouse) Color.rgb(220, 38, 38) else Color.rgb(37, 99, 235) // Red-600 or Blue-600
                val catLabel = if (isBlouse) {
                    Localization.get("category_blouse", language)
                } else {
                    val sub = if (measurement.subCategory == "top") {
                        Localization.get("punjabi_top", language)
                    } else {
                        Localization.get("punjabi_bottom", language)
                    }
                    sub
                }
                
                val mDate = DateUtil.formatIsoToDisplay(measurement.createdAt)
                
                val catTitlePaint = Paint().apply {
                    color = catColor
                    textSize = 13f // increased from 11f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText(catLabel, 40f, y, catTitlePaint)
                
                val datePaint = Paint().apply {
                    color = Color.rgb(100, 116, 139) // slate-500
                    textSize = 11f // increased from 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }
                val dateStrWidth = datePaint.measureText(mDate)
                canvas.drawText(mDate, 555f - dateStrWidth, y, datePaint)
                
                y += 6f
                canvas.drawLine(40f, y, 555f, y, Paint().apply {
                    color = Color.rgb(241, 245, 249) // slate-100
                    strokeWidth = 0.5f
                })
                y += 12f
                
                // Draw grid fields in 2 wide columns
                var col = 0
                var rowY = y
                
                for (key in keys) {
                    val label = MeasurementFields.getFieldLabel(key, language)
                    val valueVal = measurement.fields[key]
                    val valueStr = if (valueVal != null) {
                        val intVal = valueVal.toInt()
                        if (intVal.toDouble() == valueVal) "$intVal\"" else "$valueVal\""
                    } else "\"\""
                    
                    val colX = 40f + (col * 263f) // 2 columns layout
                    val cellWidth = 252f
                    val cellHeight = 25f
                    val rect = android.graphics.RectF(colX, rowY, colX + cellWidth, rowY + cellHeight)
                    
                    val cellBgPaint = Paint().apply {
                        color = Color.rgb(248, 250, 252) // slate-50
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                    val cellBorderPaint = Paint().apply {
                        color = Color.rgb(241, 245, 249) // slate-100
                        strokeWidth = 0.5f
                        style = Paint.Style.STROKE
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(rect, 4f, 4f, cellBgPaint)
                    canvas.drawRoundRect(rect, 4f, 4f, cellBorderPaint)
                    
                    val cellLabelPaint = Paint().apply {
                        color = Color.rgb(100, 116, 139) // slate-500
                        textSize = 10f // increased from 8.5f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        isAntiAlias = true
                    }
                    val cellValuePaint = Paint().apply {
                        color = Color.rgb(15, 23, 42) // slate-900
                        textSize = 10f // increased from 8.5f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    
                    canvas.drawText("$label: ", colX + 6f, rowY + 16.5f, cellLabelPaint)
                    val labelW = cellLabelPaint.measureText("$label: ")
                    canvas.drawText(valueStr, colX + 6f + labelW, rowY + 16.5f, cellValuePaint)
                    
                    col++
                    if (col >= 2) {
                        col = 0
                        rowY += 30f
                        y = rowY
                    }
                }
                
                if (col != 0) {
                    rowY += 30f
                    y = rowY
                }
                
                // Draw notes if present (properly wrapped)
                if (hasNotes) {
                    var tempY = y + 4f
                    canvas.drawText(mNotesLabel, 40f, tempY, labelPaintM)
                    for (i in notesLines.indices) {
                        val line = notesLines[i]
                        val drawX = if (i == 0) 40f + notesLabelW else 40f
                        canvas.drawText(line, drawX, tempY, mNotesPaint)
                        tempY += mNotesPaint.textSize * 1.3f
                    }
                    y = tempY + 4f
                }
                
                y += 10f
                canvas.drawLine(40f, y, 555f, y, Paint().apply {
                    color = Color.rgb(226, 232, 240) // Slate-200
                    strokeWidth = 0.5f
                })
                y += 18f
            }
        }

        pageBuilder.finish()

        val fileName = "Measurement_Book_${customer.name.replace(" ", "_")}.pdf"
        val file = File(context.cacheDir, fileName)
        return try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
            val testWidth = paint.measureText(testLine)
            if (testWidth > maxWidth) {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            } else {
                currentLine = StringBuilder(testLine)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    private fun measureAndDrawCustomerBox(
        canvas: Canvas?,
        startY: Float,
        customer: Customer,
        language: String,
        labelPaint: Paint,
        bodyTextPaint: Paint,
        boldValuePaint: Paint
    ): Float {
        val labelName = Localization.get("name", language)
        val labelId = Localization.get("customer_id", language)
        val labelMobile = Localization.get("mobile", language)
        val labelAltMobile = Localization.get("alt_mobile", language)
        val labelAddress = Localization.get("address", language)
        val labelNotes = Localization.get("notes", language)
        val labelJoined = Localization.get("joined", language)
        
        val leftX = 56f
        val rightX = 320f
        val fullWidth = 480f
        
        var currentY = startY + 16f
        
        // Row 1: Name (Full width wrapping)
        val nameLabel = "$labelName: "
        val nameVal = customer.name
        val nameLines = wrapText(nameVal, boldValuePaint, fullWidth - boldValuePaint.measureText(nameLabel))
        
        if (canvas != null) {
            var tempY = currentY
            canvas.drawText(nameLabel, leftX, tempY, labelPaint)
            val labelW = labelPaint.measureText(nameLabel)
            for (line in nameLines) {
                canvas.drawText(line, leftX + labelW, tempY, boldValuePaint)
                tempY += boldValuePaint.textSize * 1.3f
            }
        }
        currentY += (nameLines.size * boldValuePaint.textSize * 1.3f).coerceAtLeast(18f)
        
        // Row 2: Customer ID (Left) & Joined (Right)
        val idLabel = "$labelId: "
        val idVal = customer.customerId
        val joinedLabel = "$labelJoined: "
        val joinedVal = DateUtil.formatIsoToDisplay(customer.createdAt).split(",").firstOrNull() ?: ""
        
        if (canvas != null) {
            canvas.drawText(idLabel, leftX, currentY, labelPaint)
            canvas.drawText(idVal, leftX + labelPaint.measureText(idLabel), currentY, boldValuePaint)
            
            canvas.drawText(joinedLabel, rightX, currentY, labelPaint)
            canvas.drawText(joinedVal, rightX + labelPaint.measureText(joinedLabel), currentY, bodyTextPaint)
        }
        currentY += 18f
        
        // Row 3: Mobile (Left) & Alt Mobile (Right, if present)
        val mobLabel = "$labelMobile: "
        val mobVal = customer.mobile
        
        if (canvas != null) {
            canvas.drawText(mobLabel, leftX, currentY, labelPaint)
            canvas.drawText(mobVal, leftX + labelPaint.measureText(mobLabel), currentY, bodyTextPaint)
            
            if (!customer.alternateMobile.isNullOrBlank()) {
                val altLabel = "$labelAltMobile: "
                val altVal = customer.alternateMobile
                canvas.drawText(altLabel, rightX, currentY, labelPaint)
                canvas.drawText(altVal, rightX + labelPaint.measureText(altLabel), currentY, bodyTextPaint)
            }
        }
        currentY += 18f
        
        // Row 4: Address (Full width wrapping, if present)
        if (!customer.address.isNullOrBlank()) {
            val addrLabel = "$labelAddress: "
            val addrVal = customer.address
            val addrLines = wrapText(addrVal, bodyTextPaint, fullWidth - bodyTextPaint.measureText(addrLabel))
            
            if (canvas != null) {
                var tempY = currentY
                canvas.drawText(addrLabel, leftX, tempY, labelPaint)
                val labelW = labelPaint.measureText(addrLabel)
                for (i in addrLines.indices) {
                    val line = addrLines[i]
                    val drawX = if (i == 0) leftX + labelW else leftX
                    canvas.drawText(line, drawX, tempY, bodyTextPaint)
                    tempY += bodyTextPaint.textSize * 1.3f
                }
            }
            currentY += (addrLines.size * bodyTextPaint.textSize * 1.3f).coerceAtLeast(18f)
        }
        
        // Row 5: Notes (Full width wrapping, if present)
        if (!customer.notes.isNullOrBlank()) {
            val notesLabel = "$labelNotes: "
            val notesVal = customer.notes
            val notesLines = wrapText(notesVal, bodyTextPaint, fullWidth - bodyTextPaint.measureText(notesLabel))
            
            if (canvas != null) {
                var tempY = currentY
                canvas.drawText(notesLabel, leftX, tempY, labelPaint)
                val labelW = labelPaint.measureText(notesLabel)
                for (i in notesLines.indices) {
                    val line = notesLines[i]
                    val drawX = if (i == 0) leftX + labelW else leftX
                    canvas.drawText(line, drawX, tempY, bodyTextPaint)
                    tempY += bodyTextPaint.textSize * 1.3f
                }
            }
            currentY += (notesLines.size * bodyTextPaint.textSize * 1.3f).coerceAtLeast(18f)
        }
        
        return currentY + 12f
    }

    private class PageBuilder(
        private val pdfDocument: PdfDocument,
        private val shopName: String,
        private val customerName: String
    ) {
        private var pageNumber = 1
        private var currentPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
            private set
        
        fun startNewPage(): Canvas {
            currentPage?.let {
                drawFooter(it.canvas)
                pdfDocument.finishPage(it)
            }
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            currentPage = page
            canvas = page.canvas
            drawPageHeader(page.canvas)
            pageNumber++
            return page.canvas
        }
        
        private fun drawPageHeader(canvas: Canvas) {
            if (pageNumber > 1) {
                val headerTextPaint = Paint().apply {
                    color = Color.rgb(100, 116, 139) // Slate-500
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }
                val linePaint = Paint().apply {
                    color = Color.rgb(226, 232, 240) // Slate-200
                    strokeWidth = 0.5f
                    isAntiAlias = true
                }
                canvas.drawText("$shopName — Customer: $customerName (Continued)", 40f, 35f, headerTextPaint)
                canvas.drawLine(40f, 42f, 555f, 42f, linePaint)
            }
        }
        
        private fun drawFooter(canvas: Canvas) {
            val footerTextPaint = Paint().apply {
                color = Color.rgb(148, 163, 184) // Slate-400
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            val linePaint = Paint().apply {
                color = Color.rgb(226, 232, 240) // Slate-200
                strokeWidth = 0.5f
                isAntiAlias = true
            }
            canvas.drawLine(40f, 800f, 555f, 800f, linePaint)
            canvas.drawText("Page ${pageNumber - 1}", 40f, 814f, footerTextPaint)
            canvas.drawText("Generated via Kalanidhan MeasureBook", 380f, 814f, footerTextPaint)
        }
        
        fun finish() {
            currentPage?.let {
                drawFooter(it.canvas)
                pdfDocument.finishPage(it)
                currentPage = null
                canvas = null
            }
        }
    }
}
