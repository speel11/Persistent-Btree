package csc365;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author speel_000
 */
//Cache currently is not very well supported.
public class Cache extends HashMap {

    private static ConcurrentHashMap<String, String> cacheMap;
    private static WebParser parser;
    private static Btree tree;
    private static boolean ready;

    public Cache() {
        cacheMap = new ConcurrentHashMap<>(5000);
        ready = false;
    }

    public static boolean websiteRequiresUpdate(String website) {
        boolean needsUpdate = false;

        try {
            URL url = new URL(website);
            URLConnection connection = url.openConnection();
            String lastmod = connection.getHeaderField("Last-Modified").replaceAll(" ", "");
            
            System.out.println(website);
            System.out.println(cacheMap.get(website));
            System.out.println(lastmod);
           
            if (!cacheMap.get(website).equalsIgnoreCase(lastmod)) //site has been updated
            {
                needsUpdate = true;
                return needsUpdate;
            }
        } catch (IOException ex) {
            System.out.println("Website could not be loaded: " + website);
        }

        return needsUpdate;
    }

    public static void update(String website) {
        try {
            URL url = new URL(website);
            URLConnection connection = url.openConnection();
            String lastmod = connection.getHeaderField("Last-Modified").replaceAll(" ", "");

            //update cacheMap with new site info.
            cacheMap.remove(website);
            cacheMap.put(website, lastmod);

            parser.reparseWebsite(website, tree);

        } catch (IOException ex) {
            System.out.println("Website could not be loaded: " + website);
        }
    }

    static {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        //System.out.println("thread 2");
                        if (ready) {
                            //System.out.println("Thread 2 ready.");
                            for (String key : cacheMap.keySet()) {
                                //System.out.println(key);
                                if (websiteRequiresUpdate(key)) {
                                    //Website is expired
                                    update(key);
                                    System.out.println(key + " requires updating");
                                }
                            }
                        }
                        Thread.sleep(TimeUnit.MINUTES.toMillis(1));

                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    public static void put(String key, String lastMod) {

        cacheMap.put(key, lastMod.replaceAll(" ", ""));
    }

    public static String lastmod(String key) {
        String s = cacheMap.get(key);
        return s;
    }

    public void read(File file) {
        try {
            Scanner sc = new Scanner(file);
            String webPage;
            String lastmod;
            String regex = "\"[^\"]+\""; //obtain a string(any alphanumeric that isn't " one or more times) inside of quotes

            while (sc.hasNextLine()) {
                webPage = sc.next();
                lastmod = sc.next(regex).replaceAll("\"", "");
                cacheMap.put(webPage, lastmod);
            }

        } catch (FileNotFoundException ex) {
            System.out.println("file not found.");
        }
    }

    public void setReadyFlag(boolean bool) {
        Cache.ready = bool;
    }

    public void setParser(WebParser parser) {
        Cache.parser = parser;
    }

    public void setTree(Btree tree) {
        Cache.tree = tree;
    }

}
