package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;


public class write{

//__________writes to excel document, adding new websites to the end of document
    public void writerNew(Object[][][] websites, String excelFilePath){
        try {

            FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);               //Get first sheet in excel document above
            int rowCount = sheet.getLastRowNum();                       
 
            for (Object[][] aSite : websites) {                         //For every object representing a site + scrape

                Row row = sheet.createRow(++rowCount);                  //Create row in end of document 
                int columnCount = 0;

                for (Object[] col : aSite) {                            
                    Cell cell = row.createCell(columnCount);
                    String s = "";

                    //format cell to allow newlines
                    CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
                    cellStyle.setWrapText(true);
                    cell.setCellStyle(cellStyle);

                    for (Object value : col) {
                        s += value + "\n";
                    }
                    cell.setCellValue((String) s);
                    columnCount++;
                }
            }
 
            inputStream.close();
 
            FileOutputStream outputStream = new FileOutputStream(excelFilePath);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
             
        } catch (IOException | EncryptedDocumentException ex) {
            ex.getMessage();
            System.out.println("An error ocurred! Make sure the chosen file is in excel format.");
        }

    }


//_____________writes to excel document, overwriting any saved data
    public void writerExcel(Object[][][] websites, String excelFilePath){

        try {

            FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);               //Get first sheet in excel document above
            int rowCount = -1;
 
            for (Object[][] aSite : websites) {

                if(aSite == null){
                    continue;
                }

                Row row = sheet.createRow(++rowCount);
                int columnCount = 0;

                for (Object[] col : aSite) {
                    Cell cell = row.createCell(columnCount);
                    String s = "";

                    //format cell to allow newlines
                    CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
                    cellStyle.setWrapText(true);
                    cell.setCellStyle(cellStyle);

                    for (Object value : col) {
                        s += value + "\n";
                    }

                    cell.setCellValue((String) s);
                    columnCount++;
                }
            }
 
            inputStream.close();
 
            FileOutputStream outputStream = new FileOutputStream(excelFilePath);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
             
        } catch (IOException | EncryptedDocumentException ex) {
            ex.printStackTrace();
            System.out.println("An error ocurred! Make sure the chosen file is in excel format & non-empty.");
        }

    }




     public void writerExcel_map(Map<String, ArrayList<HashSet<String>>> websites,  ArrayList<String> searchwords, String excelFilePath){

        try {

            FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);               //Get first sheet in excel document above
            int rowCount = -1;
 
            for (Object website : websites.keySet()) {

                if(website == null){
                    continue;
                }

                // Writes website in col 0
                Row row = sheet.createRow(++rowCount);
                int columnCount = 0;

                Cell cell = row.createCell(columnCount);
                CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
                cellStyle.setWrapText(true);
                cell.setCellStyle(cellStyle);

                cell.setCellValue((String) website);

                // Iterates HashSets in the ArrayList, which is value for the website URL (key in the map)
                for (HashSet<String> word : websites.get(website)) {
                    cell = row.createCell(++columnCount);
                    String s = "";
                    //format cell to allow newlines
                    cell.setCellStyle(cellStyle);

                    //Iterates through the words in the HashSet and builds a string for printing.
                    for (Object value : word) {
                        s += value + "\n";

                    }
                    try{
                        cell.setCellValue((String) s);

                    }catch(IllegalArgumentException e){
                        cell.setCellValue("Too many matches found. Try to narrow down your search.");
                    }
                    
                }
                cell = row.createCell(++columnCount);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  //gets current date and time
                LocalDateTime now = LocalDateTime.now();        //gets current date and time
                cell.setCellValue(dtf.format(now));
            }

            ArrayList<String> headline = new ArrayList<>();     //Creates header
            headline.add("Website");                            //First column is for websites
            headline.addAll(searchwords);                           //Next comes searchwords
            headline.add("Timestamp");                  //At last, the current time
            addColNames(headline, workbook);                //"Inject" the header in the top of document
            inputStream.close();

 
            FileOutputStream outputStream = new FileOutputStream(excelFilePath);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
             
        } catch (IOException | EncryptedDocumentException ex) {
            ex.printStackTrace();
        }

    }


//________Method used to, after reading excel and concluded using + overwriting its lines, adds a header for columns at row 0.
    public void addColNames(ArrayList<String> fields, Workbook workbook){

        try {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum(); 
            sheet.shiftRows(0, lastRow, 1, true, true);
            sheet.createRow(0);

            for (int i = 0; i < fields.size() ; i++) {

                Cell cell = sheet.getRow(0).createCell(i);
                CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
                cellStyle.setWrapText(true);

                Font font = workbook.createFont();
                font.setBold(true);
                font.setUnderline(Font.U_SINGLE);
                font.setColor(HSSFColorPredefined.DARK_RED.getIndex());
                cellStyle.setFont(font);
        
                cell.setCellStyle(cellStyle);
                cell.setCellValue((String) fields.get(i));
                
            }

        } catch (EncryptedDocumentException ex) {
            ex.printStackTrace();
            System.out.println("An error ocurred! Make sure the chosen file is in excel format & non-empty.");
        }

    }
}
