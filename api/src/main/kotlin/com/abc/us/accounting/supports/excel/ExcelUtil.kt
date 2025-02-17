package com.abc.us.accounting.supports.excel

import com.abc.us.accounting.supports.FileUtil
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.reflect.KMutableProperty

class ExcelUtil {

    companion object {
        private val logger = KotlinLogging.logger {}

        val EXCEL_EXT_LIST = listOf(
            "xlsx",
            "xls"
        )

        // 숫자를 알파벳으로 변환하는 함수
        fun convertNumberToExcelColumn(number: Int): String {
            require(number in 0..50) { "Number must be between 0 and 25" }
            return ('A' + number.toInt()).toString()
        }

        // 셀 값을 안전하게 읽고, 오류가 발생하면 셀 정보와 오류 메시지를 출력하는 함수
        fun safeReadCellValue(fieldKey: String, cell: Cell): ExcelCellDto {
            val formatter = DataFormatter()
            var cellValue: Any? = null
            var isFailed = true

            var excelMessage = "OK"
            try {
                cellValue = processCell(cell, fieldKey, formatter)
//                cellValue = formatter.formatCellValue(cell)
//                println("value : $cellValue, Row ${cell.rowIndex + 1}, Column ${cell.columnIndex + 1}, isFailed : ${!cellValue.isNullOrEmpty()}")
                if (!cellValue?.toString().isNullOrEmpty()) {
                    isFailed = false
                    excelMessage = "Failed"
                }
            } catch (e: Exception) {
                excelMessage = "[$fieldKey] ${e.message.toString()}"
                cellValue = formatter.formatCellValue(cell)
                e.printStackTrace()
                println("Error at Row ${cell.rowIndex + 1}, Column ${cell.columnIndex + 1}: ${e.message}")
            }

            return ExcelCellDto(
                cellValue = cellValue,
                rowIndex = (cell.rowIndex + 1),
                rowName = convertNumberToExcelColumn(cell.columnIndex) + "${(cell.rowIndex).toLong()}",
                columnIndex = (cell.columnIndex + 1),
                message = excelMessage,
                fieldName = fieldKey,
                isFailed = isFailed
            )
        }

        fun download(
            headers: List<String>,
            datas: List<List<Any?>>,
            fileName: String,
            response: HttpServletResponse,
            isFileNameTimestamp: Boolean = true
        ) = try {
            val newFileName = if (isFileNameTimestamp) {
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS")
                "${fileName}_${OffsetDateTime.now().format(formatter)}"
            } else {
                fileName
            }
            XSSFWorkbook().use { workbook ->
                val sheet: Sheet = workbook.createSheet(fileName)
                var rowNo = 0

                // header
                val headerRow: Row = sheet.createRow(rowNo++)
                headers.forEachIndexed { index, s ->
                    headerRow.createCell(index).setCellValue(s)
                }

                // data
                datas.forEach {
                    val row: Row = sheet.createRow(rowNo++)
                    it.forEachIndexed { index, s ->
                        if (s == null) {
                            row.createCell(index).setCellValue("NULL")
                        } else {
                            val type = s.javaClass.simpleName
                            if (type == "Double") {
                                val value = s as Double
                                row.createCell(index).setCellValue(value)
                            } else {
                                row.createCell(index).setCellValue(s.toString())
                            }
                        }
                    }
                }
//                response.contentType = "ms-vnd/excel"
                response.setHeader(
                    HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("$newFileName.xlsx", StandardCharsets.UTF_8).build().toString()
                )
                workbook.write(response.outputStream)
            }
        } catch (ex: IOException) {
            logger.error("Excel Parsing Error : $ex.message")
        }

        fun isExcel(file: MultipartFile?): Boolean {
            return FileUtil.fileValidCheck(file, EXCEL_EXT_LIST)
        }

        inline fun <reified T> getDataFromFile(
            file: MultipartFile,
            propertyKeyList: List<String>,
            excludedIsNull: Set<String>?,
        ): MutableList<ExcelRowDto<T>> {
            // 파일 확장자를 추출
            val fileExtension = file.originalFilename?.substringAfterLast('.', "")?.lowercase()
            if (fileExtension !in EXCEL_EXT_LIST) {
                throw IllegalArgumentException("Unsupported file type")
            }

            // Workbook 객체를 생성
            var workbook: Workbook?
            workbook = try {
                when (fileExtension) {
                    "xlsx" -> XSSFWorkbook(file.inputStream)
                    "xls" -> HSSFWorkbook(file.inputStream)
                    else -> throw IllegalArgumentException("Unsupported file type")
                }
            } catch (e: Exception) {
                // 파일포멧이 'xlsx' 이지만, 실제로 'xls' 형태이므로 추가함.
                HSSFWorkbook(file.inputStream).also { workbook = it }
            }

            val list = ArrayList<ExcelRowDto<T>>()
            val worksheet: Sheet? = workbook?.getSheetAt(0)
            if (worksheet != null) {
                for (i in 1 until worksheet.physicalNumberOfRows) {
                    val row = worksheet.getRow(i)
                    // 행이 null이 아니고, 데이터가 있는 경우
                    if (row != null && row.cellIterator().hasNext()) {
                        if (i - 1 < propertyKeyList.size) {
                            list.add(processRow<T>(row, i, propertyKeyList, excludedIsNull))
                        }
                    }
                }
            }
            return list
        }

        inline fun <reified T> processRow(
            row: Row,
            rowIndex: Int,
            propertyKeyList: List<String>,
            excludedIsNull: Set<String>?,
        ): ExcelRowDto<T> {
            val valueObject = T::class.java.getDeclaredConstructor().newInstance()
            val cellList = ArrayList<ExcelCellDto>()
            propertyKeyList.forEachIndexed { coulmnIndex, key ->
                // row.getCell(index) 호출 결과를 변수에 저장하고 null 체크
                val cell = row.getCell(coulmnIndex)
                if (cell != null) { // null 체크 후에 cell을 안전하게 읽기
                    val cellValue = safeReadCellValue(key, cell)
                    println("key : $key, cellValue.cellValue : ${cellValue.cellValue}, valueObject : $valueObject")
                    val property = T::class.members.find { it.name == key } as? KMutableProperty<*>
//                    var gsonCellValue = MapperUtil.gsonBuilder(key).toJson(cellValue.cellValue)
//                    println("gsonCellValue : $gsonCellValue")
                    if (cellValue?.cellValue?.toString()?.trim().isNullOrEmpty()) {
                        cellValue.cellValue = null
                    }

                    property?.setter?.call(valueObject, cellValue.cellValue)
                    if (excludedIsNull?.contains(key) == true) { // null 항목 제외 대상 컬럼
                        cellValue.isFailed = false
                        cellValue.message = "Failed"
                    }
                    cellList.add(cellValue)
                } else {
                    var isFailed = true
                    if (excludedIsNull?.contains(key) == true) { // null 항목 제외 대상 컬럼
                        isFailed = false
                    }
                    if(isFailed) { // 실패만 전달되도록 변경
                        cellList.add(
                            ExcelCellDto(
                                rowIndex = rowIndex,
                                columnIndex = coulmnIndex + 1,
                                isFailed = true,
                                fieldName = key,
                                rowName = convertNumberToExcelColumn(coulmnIndex) + "$rowIndex",
                                message = "[$key] key is null"
                            )
                        )
                    }
                }
            }

            return ExcelRowDto(valueObject, cellList)
        }

        private fun processCell(cell: Cell, key: String, formatter: DataFormatter): Any? {
            var resValue: Any? = null;
            when (val cellType = cell.cellType) {
                CellType.STRING -> {
                    val stringValue = formatData(key, formatter.formatCellValue(cell))
                    println("String: $stringValue")
                    resValue = stringValue
                }

                CellType.NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        val dateValue = cell.dateCellValue
                        if (key.endsWith("date")) {
                            val localDate = dateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                            println("LocalDate: $localDate")
                            resValue = localDate
                        } else if (key.endsWith("time")) {
                            val offsetDateTime = dateValue.toInstant()
                                .atOffset(ZoneId.systemDefault().rules.getOffset(dateValue.toInstant()))
                            println("OffsetDateTime: $offsetDateTime")
                            resValue = offsetDateTime
                        }
                    } else {
                        val numericValue = cell.numericCellValue
                        val formattedValue = DecimalFormat("##0.###").format(numericValue)
                        println("Double: $formattedValue")
                        resValue = formattedValue
                    }
                }

                else -> {
                    val stringValue = formatData(key, formatter.formatCellValue(cell))
                    println("Unsupported cell type: $stringValue")
                    resValue = stringValue
                }
            }
            return resValue
        }


        private fun formatData(key: String, value: String): String? {
            // 날짜 형식 검사 (yyyy-MM-dd)
            if (value.matches(Regex("^\\d{4}-\\d{2}-\\d{2}\$")) || key.lowercase().endsWith("date")) {
                val dateValue = LocalDate.parse(value)
                val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//                println("형식화된 날짜: ${dateValue.format(dateFormat)}")
                return dateValue.format(dateFormat)// 날짜 형식화 후 종료
            }

            // 날짜와 시간 형식 검사 (yyyy-MM-dd HH:mm:ss)
            if (value.matches(Regex("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\$")) || key.lowercase()
                    .endsWith("time")
            ) {
                val dateTimeValue = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//                println("형식화된 날짜와 시간: ${dateTimeValue.format(dateTimeFormat)}")
                return dateTimeValue.format(dateTimeFormat)// 날짜와 시간 형식화 후 종료
            }

            // 숫자 값 검사 (Double)
            if (value.matches(Regex("^[0-9]+(\\.[0-9]+)?\$"))
                || (key.lowercase().endsWith("amount") || key.lowercase().endsWith("price"))
            ) {

                var numericValue:Double = 0.0
                if(!value.isNullOrEmpty()){
                    numericValue = value.toDouble()
                }
                val doubleFormat = DecimalFormat("##0.###")
//                println("형식화된 실수: ${doubleFormat.format(numericValue)}")
                return doubleFormat.format(numericValue)// 실수 형식화 후 종료
            }

            // 정수 값 검사 (Int)
            if (value.matches(Regex("^[0-9]+\$")) || key.lowercase().endsWith("quantity")) {
                var intValue = 0
                if(!value.isNullOrEmpty()){
                    intValue = value.toInt()
                }
                val formattedInt = String.format("%,d", intValue)
//                println("형식화된 정수: $formattedInt")
                return formattedInt// 정수 형식화 후 종료
            }
            if (value.isEmpty()) {
                return null
            }
            return value
        }

    }
}