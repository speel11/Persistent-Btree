/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author speel_000
 */
public class WebParser {

    
    private ArrayList<HashEntry[]> strings;
    private ArrayList<String> websites = new ArrayList<>();
    private ArrayList<String> relatedSites;
    private Cache cacheMap;
    private String wiki = "http://en.wikipedia.org";
    private File aFile;
    private RandomAccessFile file;
    private PrintWriter writer;
    private Btree<String, HashTable> bt;

    int index = -1;

    public WebParser() {
       
        strings = new ArrayList<>();
        relatedSites = new ArrayList<>();
        cacheMap = new Cache();

        try {
            file = new RandomAccessFile("btree.txt", "rw");

            bt = new Btree<>(file);
        } catch (FileNotFoundException ex) {
            System.out.println("btree.txt not found");
        }
    }

    public void parse(ArrayList<String> names) throws FileNotFoundException, UnsupportedEncodingException {

        if (bt.needToCreate) {
            aFile = new File("cache.txt");
            writer = new PrintWriter(aFile);
            websites.addAll(names);

            for (String webPage : names) {

               
                cacheMap.put(webPage, 100000);

               
                writer.println(webPage + " " + System.currentTimeMillis());
                try {
                    Document doc = (Document) Jsoup.connect(webPage).get();
                    Elements paragraphs = doc.select("p");
                    //tmp = new HashTable();
                    for (Element p : paragraphs) {
                        Elements links = p.select("[href]");
                        for (Element link : links) {
                            if (link.attr("href").startsWith("/wiki/") && !link.attr("href").contains(":")) {
                                if (!relatedSites.contains(wiki + link.attr("href")) && !websites.contains(wiki + link.attr("href"))) {
                                    relatedSites.add(wiki + link.attr("href"));
                                }
                            }
                        }
                        String[] result = p.text().split("\\W");
                        for (String word : result) {
                            if (word.length() > 4) {
                                bt.put(word.toLowerCase(), webPage, 1);
                            }
                        }
                    }
                } catch (IOException ex) {
                    System.out.println("IO Exception");
                }
            }
            writer.flush();
            try {
                parseRelated();
            } catch (IOException ex) {
                Logger.getLogger(WebParser.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            aFile = new File("cache.txt");
            cacheMap.read(aFile);
            bt = bt.readFile(cacheMap, this);

        }
    }

    private void parseRelated() throws IOException {
        int c = 0;
        System.out.println(relatedSites.size());
        for (String webPage : relatedSites) {
            cacheMap.put(webPage, 100000);
            writer.println(webPage + " " + System.currentTimeMillis());

            System.out.println((c++) + "--" + webPage);
            try {
                Document doc = (Document) Jsoup.connect(webPage).get();
                Elements paragraphs = doc.select("p");
                for (Element p : paragraphs) {
                    String[] result = p.text().split("\\W");

                    for (String word : result) {
                        //System.out.println(word);
                        if (word.length() > 4) {
                            bt.put(word.toLowerCase(), webPage, 1);
                        }
                    }

                }
            } catch (HttpStatusException ex) {
                System.out.println("404 error: Website cannot be reached: " + ex.getUrl());
            } catch (SocketTimeoutException st) {
                System.out.println("Timed out: " + webPage);
            }

        }

        bt.write();
        writer.close();
    }

    public String check(String name) {
        String relatedName;
        String topsite = "";
        HashTable h;
        int n = 0;
        HashTable table = new HashTable();
        
        try {
            Document doc = (Document) Jsoup.connect(name).get();
            Elements paragraphs = doc.select("p");
            for (Element p : paragraphs) {
                String[] result = p.text().split("\\W");
                for (String word : result) {
                    if (word.length() > 4) {
                        n = 0;
                        //System.out.println(word);
                        h = bt.search(word.toLowerCase());

                        if (h != null) {
                            for (int i = 0; i < h.size(); i++) {
                                if (h.get(i) != null) {
                                    int t = h.getFrequency(i);
                                    String site = h.getWebsite(i);

                                    if (n < t) {
                                        n = t;
                                        topsite = site;
                                    }
                                }
                            }
                            
                            if (table.containsWebsite(topsite)) 
                                table.increment(topsite);
                            else
                                table.put(topsite, n);
                            
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("IO Exception");
        }

        String topPages[] = new String[3];
        int max[] = new int[3];

        for (int i = 0; i < table.size(); i++) {
            if (table.get(i) != null) {
                int freq = table.getFrequency(i);
                String page = table.getWebsite(i);
                if (freq > max[0]) {
                    max[0] = freq;
                    topPages[2] = topPages[1];
                    topPages[1] = topPages[0];
                    topPages[0] = page;

                } else if (freq < max[0] && freq > max[1]) {
                    max[1] = freq;
                    if (!page.equals(topPages[0])) {
                        topPages[2] = topPages[1];
                        topPages[1] = page;
                    }

                } else if (freq < max[1] && freq > max[2]) {
                    max[2] = freq;
                    if (!(page.equals(topPages[0])) && !(page.equals(topPages[1]))) {
                        topPages[2] = page;
                    }
                }
            }
        }

        relatedName = topPages[0] + "\n" + topPages[1] + "\n" + topPages[2] + "\n";

        return relatedName;
    }

    public void reparseWebsite(String website, Btree<String, HashTable> temp) {
//        cacheMap.remove(website);
//        cacheMap.put(website, 100000);
        try {

            Document doc = (Document) Jsoup.connect(website).get();
            Elements paragraphs = doc.select("p");

            for (Element p : paragraphs) {
                String[] result = p.text().split("\\W");

                for (String word : result) {
                    if (word.length() > 4) {
                        temp.put(word.toLowerCase(), website, 1);
                        
                    }
                }
            }

        } catch (IOException ex) {
            System.out.println("IO Exception");
        }
    }

    public ArrayList getStrings() {
        return strings;
    }
}
