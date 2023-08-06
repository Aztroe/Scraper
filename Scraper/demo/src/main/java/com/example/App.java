package com.example;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JPanel;  
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import java.awt.*;  
import java.awt.event.*;  
import java.lang.Exception;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;

import org.apache.logging.log4j.core.tools.picocli.CommandLine.ExecutionException;
import org.apache.poi.EmptyFileException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.*;  

import java.io.OutputStream;
import java.io.PrintStream;


class CreateInputForm extends JFrame  
{  
    JButton insert_entries, fill_excel, search_terms;  
    JPanel newPanel;  
    JLabel userLabel;  
    JTextArea textArea;
    
    //calling constructor  
    CreateInputForm()  
    {     
        //create instructions
        setSize(700,700); 
        userLabel = new JLabel();  
        userLabel.setText("<html> <h2>This is a website scraper. </h2> <br> - To scrape websites: <br> Paste the full URL into the text window below. <br> Then choose the target excel document to write into by clicking 'Submit new Entries'. <br> For multiple URLs, please seperate them by a newline (Enter) and a newline only. <br> <br> - To scrape all websites in an excel document: <br> Fill the first column with desired URLs in an excel document, and choose this document by clicking 'Run by excel document'.<br> PLEASE NOTE: This method will overwrite any saved data in chosen document. <br> <br> - To find select terms on websites: <br> Click 'Search for terms'. <br> First, choose an excel document with chosen words written into the first column. One word per row. <br> Second, choose an excel document with websites in question. One website per row, first column. <br> PLEASE NOTE: This method will overwrite any saved data in chosen document.</html>");      //set label value for textField1 
        userLabel.setPreferredSize(new Dimension(300, 400));
          
        //create text area to retrieve input  
        textArea = new JTextArea(8, 15);    //set length of the text  
          
        //create submit button  
        insert_entries = new JButton("SUBMIT NEW ENTRIES"); //set label to button
        fill_excel = new JButton("RUN BY EXCEL DOCUMENT");  
        search_terms = new JButton("SEARCH FOR TERMS");
          
        //create panel to put form elements  
        newPanel = new JPanel(); 
        newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS)); 
        newPanel.add(userLabel);   
        newPanel.add(new JScrollPane(textArea));    

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(insert_entries);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(fill_excel);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(search_terms);
          
        
        add(newPanel, BorderLayout.CENTER);         //set border to panel 
        add(buttonPane, BorderLayout.PAGE_END);
        

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        

//Perform action on button click: "Submit new entries"_______Scrapes phone numbers and e-mails off of websites in text area, adds to chosen excel document_________________________________________ 
        insert_entries.addActionListener(new ActionListener() {    
        
            public void actionPerformed(ActionEvent ae){   
                Thread queryThread = new Thread() {
                    public void run() {

                        final JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());    //Create a file chooser
                        int returnVal = fc.showOpenDialog(null);        //In response to a choice


                        if (returnVal == JFileChooser.APPROVE_OPTION) {         //If choice is valid:

                            long start = System.currentTimeMillis();            //Measure time
                            errorwin errors_window = new errorwin();
                            
                            File selectedFile = fc.getSelectedFile();
                            String excelFilePath = selectedFile.getAbsolutePath();
                        
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  //gets current date and time
                            LocalDateTime now = LocalDateTime.now();        //gets current date and time
                    
                            ArrayList<String[][]> arr = new ArrayList<>();          //Create arraylist
                            Scanner scan = new Scanner(textArea.getText());         //Scan the input from user
                            String[][][] array = {{{}}};                            //Empty array
                    
                            soupscrape scraper = new soupscrape();
                            errors_window.pbar.setMaximum(1);
                            errors_window.setVisible(true);

                            while (scan.hasNextLine()) {
                                errors_window.pbar_update(0);
                                String line = scan.nextLine();                      //Scan next line, ie. next URL
                                scraper.errormessage("Scraping " + line + "...");
                                String[][] site = {{}};
                                try {
                                    site = scraper.scrape_all(line, dtf.format(now).toString());     //Format URL with its scrapings and formatted time
                                } catch (ExecutionException | InterruptedException e) {
                                    scraper.errormessage("Execution was interrupted: \n");
                                    e.printStackTrace();
                                }                                                           
                                arr.add(site);                                      //Add to arraylist
                                errors_window.pbar_update(1);
                            }
                    
                            scan.close();
                            array = arr.toArray(array);                             //Convert arraylists contents to the normal array, arrays cant add new objects
                                                                                    //dynamically. We want writer to handle a normal array for simplicity.
                            write writer = new write();
                            writer.writerNew(array, excelFilePath);                                   //Create writer that writes to EON.xslx. It will handle every formatted URL in the array.
                            
                            long finish = System.currentTimeMillis();
                            long timeElapsed = finish - start;
                            System.out.println("-------------------------Finished-------------------------");
                            System.out.println("Time elapsed : " + timeElapsed/60000.0 + " minutes.");
                        }

                    }
                };
                queryThread.start();
            }
        });


//Perform action on button click: "Run by excel document"_______Scrapes phone numbers and e-mails off of websites from excel document_________________________________________
        fill_excel.addActionListener(new ActionListener() {    
        
            public void actionPerformed(ActionEvent ae){
                Thread queryThread = new Thread() {
                    public void run() {

                        
                        final JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());    //Create a file chooser
                        int returnVal = fc.showOpenDialog(null);        //In response to a button click

                        if (returnVal == JFileChooser.APPROVE_OPTION) {

                            long start = System.currentTimeMillis();  //MÄTA EXEKVERINGSTID

                            File selectedFile = fc.getSelectedFile();
                            String excelFilePath = selectedFile.getAbsolutePath();
                            try{
                                errorwin errors_window = new errorwin();


                                FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
                                Workbook workbook = WorkbookFactory.create(inputStream);
                                Sheet sheet = workbook.getSheetAt(0);
            
                                //SKA HÄMTA EXCEL, FÖRSTA COLUMNEN
                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  //gets current date and time
                                LocalDateTime now = LocalDateTime.now();        //gets current date and time
                        
                                ArrayList<String[][]> arr = new ArrayList<>();          //Create arraylist
                                String[][][] array = {{{}}};                            //Empty array

                                soupscrape scraper = new soupscrape();                  //Create soupscrape object for scraping sites

                                errors_window.pbar.setMaximum(sheet.getLastRowNum());
                                errors_window.setVisible(true);

                                Row row = null;
                                
                                
                                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                                        row = sheet.getRow(rowIndex);
            
                                        if (row != null) {
                                        Cell cell = row.getCell(0);
                                            if (cell != null) {
                
                                                String URL = cell.getStringCellValue();     // Found column and there is value in the cell.

                                                try {
                                                    String[][] site = {{}};
                                                    site = scraper.scrape_all(URL.trim(), dtf.format(now).toString());
                                                    arr.add(site);
                                                } catch (ExecutionException | InterruptedException e) {
                                                    scraper.errormessage("Execution was interrupted");
                                                    e.printStackTrace();
                                                }    

                                            }
                                        }
                                        errors_window.pbar_update(rowIndex);
                                    }
                                
                                inputStream.close();
                                array = arr.toArray(array);                             //Convert arraylists contents to the normal array, arrays cant add new objects
                                                                                        //dynamically. We want writer to handle a normal array for simplicity.
                                write writer = new write();
                                writer.writerExcel(array, excelFilePath);        //Create writer that writes to EON.xslx. It will handle every formatted URL in the array.
                                
                                long finish = System.currentTimeMillis();
                                long timeElapsed = finish - start;
                                System.out.println("-------------------------Finished-------------------------");
                                System.out.println("Time elapsed : " + timeElapsed/60000.0 + " minutes.");

                            }catch(EmptyFileException | EncryptedDocumentException | IOException ex){
                                ex.printStackTrace();
                                System.out.println("Error occured! Check your input and make sure such a file exists!");
                            }
                        }


                    }
                };
                queryThread.start();
            }
        });

//Perform action on button click: "Search for terms"______Finds occurrences of words in first excel document, in the websites in the second excel document___________________________________________
        search_terms.addActionListener(new ActionListener() {    
        
            public void actionPerformed(ActionEvent ae){
                Thread queryThread = new Thread() {
                    public void run() {

                        //______Part 1: retrieve words to search, from excel document. Add words to list.___________
                        final JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());    //Create a file chooser
                        int returnVal = fc.showOpenDialog(null);        //In response to a button click

                        if (returnVal == JFileChooser.APPROVE_OPTION) {         //If valid choice

                            File selectedFile = fc.getSelectedFile();           
                            String excelFilePath = selectedFile.getAbsolutePath();
                            try{

                                FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
                                Workbook workbook = WorkbookFactory.create(inputStream);            //Workbook is basically a mutable (for this program) excel object
                                Sheet sheet = workbook.getSheetAt(0);

                                ArrayList<String> searchwords = new ArrayList<String>();            //List for the search words

                                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                                    Row row = sheet.getRow(rowIndex);
            
                                    if (row != null) {
                                    Cell cell = row.getCell(0);
                                        if (cell != null) {
                                            searchwords.add(cell.getStringCellValue().trim());      //Words retrieved here                                      
                                        }
                                    }
                                }
                                inputStream.close();

                                //______Part 2: pick excel document with websites in it__________
                                returnVal = fc.showOpenDialog(null);

                                if (returnVal == JFileChooser.APPROVE_OPTION) {
                                    selectedFile = fc.getSelectedFile();
                                    excelFilePath = selectedFile.getAbsolutePath();

                                    try{
                                        long start = System.currentTimeMillis();  //measure time for execution. Start value.

                                        inputStream = new FileInputStream(new File(excelFilePath));
                                        workbook = WorkbookFactory.create(inputStream);
                                        sheet = workbook.getSheetAt(0);

                                        soupscrape scraper = new soupscrape();      //scraping class object
                                        errorwin errors_window = new errorwin();      //progress window
                                        errors_window.pbar.setMaximum(sheet.getLastRowNum());
                                        //Map that holds count. Every website (key) will hold arraylist (value), which in turn holds Set respectively for searchwords.
                                        Map<String, ArrayList<HashSet<String>>> map = new HashMap<String, ArrayList<HashSet<String>>>();    
                                                                                        
                                        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) { //Iterate through sites in the excel document
                                            Row row = sheet.getRow(rowIndex);
                    
                                            if (row != null) {
                                            Cell cell = row.getCell(0);
                                                if (cell != null) {

                                                    String URL = cell.getStringCellValue().trim();     // Found column and there is a value in the cell.

                                                    //OBSERVE: in case it finds a duplicate URL in the line below, the program will, as it stands, overwrite existing
                                                    //map-entry with an empty hashset. This due to a new one being created here and all the sublinks are explored.
                                                    //Fix.
                                                    map.put(URL, new ArrayList<HashSet<String>>());     //Create key : value for URL

                                                    for(int i = 0; i < searchwords.size(); i++){
                                                        map.get(URL).add(new HashSet<String>());        // Fill arraylist (value), 1 HashSet per search word
                                                    }

                                                    scraper.get_links(URL, map, searchwords, URL);       // Method for registering matches. Fills out the map.

                                                }
                                            }
                                            errors_window.pbar_update(rowIndex);        //Updates progressbar per every URL
                                        }
                                        inputStream.close();

                                        write writer = new write();
                                        writer.writerExcel_map(map, searchwords, excelFilePath);        //Write results to excel document 2. TOO LONG
                                
                                        long finish = System.currentTimeMillis();
                                        long timeElapsed = finish - start;
                                        System.out.println("-------------------------Finished-------------------------");
                                        System.out.println("Time elapsed : " + timeElapsed/60000.0 + " minutes.");

                                    }catch(EmptyFileException | EncryptedDocumentException | IOException ex){
                                        ex.printStackTrace();
                                    }

                                }

                            }catch(EmptyFileException | EncryptedDocumentException | IOException ex){
                                ex.printStackTrace();
                            }

                        }
                    }
                };
                queryThread.start();
            }
            
        });
        setTitle("Scraper");         //set title to the form  
    }  
}


//__________________Class for progress window, which includes a progressbar and text window for errors and current URL iterating._______________
class errorwin extends JFrame  
{  
    public JTextArea printlog;
    public JProgressBar pbar;
    //constructor  
    errorwin()  
    {  
        setDefaultCloseOperation(javax.swing.  
        WindowConstants.DISPOSE_ON_CLOSE);  
        setTitle("Progress Log");  
        setSize(1000, 400);

        printlog = new JTextArea(50, 10);
        printlog.setEditable(false);

        pbar = new JProgressBar();
        pbar.setMinimum(0);
        pbar.setValue(0);
        pbar.setSize(new Dimension(80,20));
        pbar.setStringPainted(true);

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.anchor = GridBagConstraints.WEST; 
        add(pbar, constraints);        

        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
         
        add(new JScrollPane(printlog), constraints); 
        PrintStream out = new PrintStream(new CustomOutputStream(printlog), true);  //Prints/errors are set to show up in progress window
        System.setOut(out);
        System.setErr(out);
        setVisible(true);
    }

    public void pbar_update(int i) {        //Sets value in progress bar, used to update it
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            pbar.setValue(i);
          }
        });
      }
    
}

//______________________Class that is used to redirect all System.out.println to the user interface, for errormessages and updates.
class CustomOutputStream extends OutputStream {

    private JTextArea textArea;
        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }
        
        @Override
        public void write(int b) throws IOException {
            // redirects data to the text area
            textArea.append(String.valueOf((char)b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
}




//create the main class  
class App{                              //Initiates the whole program
    public static void main(String arg[])  
    {  
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try  
                {  
                    //create instance of the CreateInputForm (first window that pops up) 
                    new CreateInputForm().setVisible(true);  //make form visible to the user;  
 
                }  
                catch(Exception e)  
                {     
                    //handle exception   
                    JOptionPane.showMessageDialog(null, e.getMessage());  
                }
            }
        });
  
    } 
}
