package com.ai.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ExcelUtil {
    /**
     * @param headers 表格标题行
     */
    public static ResponseEntity genExcel(String[] headers, List<List<String>> data){
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                XSSFWorkbook workbook = new XSSFWorkbook()
        ) {
            Sheet sheet = workbook.createSheet("record");
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            // 填充数据
            int rowNum = 1;

            for (List<String> d : data) {
                Row row = sheet.createRow(rowNum++);
                int col = 1;
                for (String value: d){
                    if (value != null){
                        row.createCell(col -1).setCellValue(value);
                    }
                    col ++;
                }
            }
            workbook.write(byteArrayOutputStream);
            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=record.xlsx");
            headers1.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers1.add(HttpHeaders.PRAGMA, "no-cache");
            headers1.add(HttpHeaders.EXPIRES, "0");
            return ResponseEntity
                    .ok()
                    .headers(headers1)
                    .contentLength(byteArrayOutputStream.size())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
