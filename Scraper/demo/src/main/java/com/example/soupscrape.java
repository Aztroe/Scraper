package com.example;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.apache.logging.log4j.core.tools.picocli.CommandLine.ExecutionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.SwingUtilities;



public class soupscrape {

    public static Set<String> uniqueURL = new HashSet<String>();  //Used for get_links below. Keeps visited URLs here.

//_______Scrapes all e-mails from given doc
    public Set<String> scrape_Emails(Document doc){

            Set<String> EMAILSSET = new HashSet<String>();
            
            String email_regex = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+"; //regex for emails
            Pattern emailpattern = Pattern.compile(email_regex);       //regex above, compiled
                    
            Elements Email_check = doc.getElementsMatchingOwnText(emailpattern); //match regex in doc

            //------------------------EXTRACT EMAILS FROM BODY TEXTS-----------------------------
            for(Element e : Email_check){
                Matcher matcher = emailpattern.matcher(e.text());
                if (matcher.find()) {

                    EMAILSSET.add(matcher.group(0).trim());
                }
            }
            return EMAILSSET;   
    }
    
    
//_______Scrapes all phone numbers from given doc
    public Set<String> scrape_PhoneNumbers(Document doc){

            Set<String> pnums = new HashSet<String>();

            String pnums_regex = "[+0][-\\s\\dâ€“]{9,14}";     //regex for phone numbers
            Pattern pnumpattern = Pattern.compile(pnums_regex);     //regex above, compiled

            Elements Phone_check = doc.getElementsMatchingOwnText(pnumpattern);

            //---------------------------EXTRACT PHONE NUMBERS FROM BODY TEXTS---------------------------
            for(Element p : Phone_check){
        
                Matcher matcher = pnumpattern.matcher(p.text().replaceAll("\\s",""));
                String text = p.text().replaceAll("\\s","");
                int i = 0;
            
                while(matcher.find()){          //further filtering any numbers to make sure its a phone number
                        if(text.charAt(i) == '0' || text.charAt(i) == '+'){
                            if(i == 0){
                                pnums.add(matcher.group(0).trim());
                        }
                        else if(!Character.isDigit(text.charAt(i-1))){
                            pnums.add(matcher.group(0).trim());
                        }   
                    }
                    i++;
                    matcher = pnumpattern.matcher(text.substring(i));
                }
            }

            return pnums;
    }


//_________Simply extracts any links which has filetype pdf or doc
    public Set<String> scrape_Pdfs(Document doc){

        Elements links = doc.select("a[href]");
        Set<String> pdfs = new HashSet<String>();

        for(Element link: links){

            String linkstr = link.attr("href").toString();

            if(linkstr.indexOf(".pdf") >= 0  || linkstr.indexOf(".doc") >= 0){

                pdfs.add(link.attr("href"));
            }
        }

        return pdfs;
    }

//____________Searches for given word in given doc
    public boolean Search_Word(Document doc, String word){      

        String word2pattern = "(.*)" + word + "(.*)";
        Pattern pattern = Pattern.compile(word2pattern);       //regex above, compiled
        Elements Word_matches = doc.getElementsMatchingOwnText(pattern); //match regex in doc

        if(Word_matches.isEmpty()){
            return false;
        }else{
            return true;
        }
        
}
    //______________________________________________________________________________________________________________________________________
    //_________Scrapes all emails and phone numbers from given URL and next layer of links. Used by button 2: "Run by excel".
    //______________________________________________________________________________________________________________________________________
    //This method has input parameter: website URL and current time, and returns: array with URL along with its scraped phone numbers, emails and pdfs.
    //Improvement: make it operate against a set with visited URLS, so we can traverse further down the layers of links.
    // Currently, we only traverse through layer 1 and 2. An eventual followup where the write_excel method is optimized/simplified. 
    public String[][] scrape_all(final String URL, String time) throws ExecutionException, InterruptedException {

        String dom = "";
        if(URL.indexOf("http") < 0){                        //Add safe connection (https) to URL string in case its missing
            dom = "https://";
        }

        try{    
            Document doc = Jsoup.connect(dom + URL).get();      //* Try - Establish internet connection and retrieve html document
            
            Set<String> pnums = scrape_PhoneNumbers(doc);       //Scrape for phone numbers, add to a new set for phone numbers
            Set<String> emails = scrape_Emails(doc);            //Scrape for emails, add to a new set for emails
            Set<String> pdfs = scrape_Pdfs(doc);                //Scrape for pdf and docx, add to a set for files in these formats

            Elements links = doc.select("a[href]");     //Select all link objects in html document
            List<String> urls = links.eachAttr("href");     //Extract the "link content" from selected objects, to a url list

            urls.parallelStream().forEach(url -> {             //Lambda, for each object in url list:

                //Filter out non-sublinks (if link is not a file:)
                if(url.indexOf(".pdf") < 0 && url.indexOf(".img") < 0 && url.indexOf(".jpg") < 0 && url.indexOf(".doc") < 0 && url.indexOf("share") < 0){

                    if(url.indexOf(URL) >= 0){ //Filter out sublinks leading outside the site domain (if sublink is within domain:)
                        try {
                            final Document subdoc = Jsoup.connect(url).timeout(60 * 1000).get();        //**Try - Establish connection to sublink and retrieve html
                            pnums.addAll(scrape_PhoneNumbers(subdoc));                                  //Scrape sublink html for phone numbers, add to set for phone numbers
                            emails.addAll(scrape_Emails(subdoc));                                       //Scrape sublink html for emails, add to set for emails
                            pdfs.addAll(scrape_Pdfs(subdoc));                                           //Scrape sublink html for emails, add to set for emails
                                    
                        } catch (IllegalArgumentException | IOException e) {

                            errormessage("Sublink not connectable:  " + url + "\n");                    //**Catch - returns error message if sublink connection fails
                            e.getCause();
                        }
                    }else if (url != "" && url.charAt(0) == '/' && url.length() >= 2){ //Filter for sublinks starting with '/' and format to full URL (if sublink starts with '/':)
                        
                        //Sätt länken efter huvudlänk, annars ska det efter huvudlänk + sublink
                        
                        int index = 0;
                        if(URL.charAt(URL.length()-1) == '/'){          //Formats correctly with multiple '/' in mind
                            index = 1;
                        }

                        try{
                            final Document subdoc = Jsoup.connect(URL + url.substring(index)).timeout(60 * 1000).get();
                            pnums.addAll(scrape_PhoneNumbers(subdoc));
                            emails.addAll(scrape_Emails(subdoc));           //Scrape once again, now on current link layer (2)
                            pdfs.addAll(scrape_Pdfs(subdoc));
                                    
                        } catch (IOException e) {
                            errormessage("Sublink not connectable:  " + URL + url + "\n");
                            e.getCause();
                            
                        }
                    }
        }});

            String[] pnums_done = new String[pnums.size()];     //Create empty string arrays for phone numbers, emails and files respectively
            String[] emails_done = new String[emails.size()];
            String[] pdfs_done = new String[pdfs.size()];
    
            //Fill arrays with objects from sets containing scrapings, store arrays within a new formatted array.
            String[][] formatted = {{URL}, pnums.toArray(pnums_done) , emails.toArray(emails_done), pdfs.toArray(pdfs_done), {time}};
    
            return formatted;


        }catch (IOException error){         //* Catch - Returns error if domain is unreachable or unsafe, using the initial URL

            error.getCause();
            errormessage("Faulty/unsafe URL input: " + dom + URL);

            String[][] formatted = {{URL}, {"Faulty URL input / not safe"} , {"Faulty URL input / not safe"}, {"Faulty URL input / not safe"}, {time}};
            return formatted;
        }
    }


//___________________________________________________________________________________________________________________________________________________    
//________Recursive method that traverses through all sublinks and searches for given search words. Used by button 3, "Search for terms".____________
//___________________________________________________________________________________________________________________________________________________  
        public void get_links(String URL, Map<String, ArrayList<HashSet<String>>> map, ArrayList<String> wordMap, String website_home) {
            String dom = "";
            if(URL.indexOf("http") < 0){                        //Add safe connection (https) to URL string in case its missing
                dom = "https://";
            }

            try {
                Document doc = Jsoup.connect(dom + URL).timeout(60 * 1000).get();      //* Try - Establish internet connection and retrieve html document'
                for(String word : wordMap){        //For every word (key) in the map
                    if(Search_Word(doc, word)){             //If word (key) is in document
                        map.get(website_home).get(wordMap.indexOf(word)).add(dom + URL);     //Add URL to designated HashSet with matches for that word                  
                    }                                                                        //There are independent HashSets for every search word in every URL arraylist
                }

                errormessage(dom + URL);

                Elements links = doc.select("a[href]");

                List<String> urls = links.eachAttr("href");     //Every sublink on current site

                urls.parallelStream().forEach((this_url) -> {   //testar parallelstream
                    boolean add = uniqueURL.add(this_url);      // Check if url has been visited before
                    //Condition below filters unwanted file formats for links. We only want sites.
                    if(add && this_url.indexOf(".pdf") < 0 && this_url.indexOf(".img") < 0 && this_url.indexOf(".jpg") < 0 && this_url.indexOf(".doc") < 0 && this_url.indexOf("share") < 0 && this_url.indexOf("#") < 0){

                        if(this_url.contains(website_home)){    //Case 1: within domain, main URL with additional url slug
                            
                            get_links(this_url, map, wordMap, website_home);

                        // Case 2: sublink is linked simply as '/blabla', handled by adding sublink to main link, shaping it into case 1.
                        }else if (this_url != "" && this_url.charAt(0) == '/' && this_url.length() >= 2){ //Filter for sublinks starting with '/' and format to full URL (if sublink starts with '/':)

                            //handles link to top-level layer on website
                            int index = 0;
                            if(website_home.charAt(website_home.length()-1) == '/'){          //Make sure only one '/' is writte between new url slug and main url
                                index = 1;
                            }
                            get_links(website_home + this_url.substring(index), map, wordMap, website_home);
                        }else{             //Case 3: handles link to current layer on website. Anything that isnt a link to another website
                            String addslash = "";
                            if(URL.charAt(URL.length()-1) != '/'){  
                                addslash = "/";
                            }
                            get_links(URL + addslash + this_url, map, wordMap, website_home);
                        }
                    }
                });
    
            } catch (IOException | IllegalArgumentException ex) {
                ex.getCause();
            }
        }



//________Method for enabling printing to progress window, mid-process. Look into threads and printing.
    public void errormessage(final String Line) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            System.out.println(Line);
          }
        });
      }


}
