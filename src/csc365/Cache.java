package csc365;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
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
public class Cache extends HashMap {

    private static ConcurrentHashMap<String, Entry> cacheMap;

    public Cache() {
        cacheMap = new ConcurrentHashMap<>();

    }

    static class Entry {

        private int lastAccessed;

        private final long expiration;

        public Entry(long expire) {

            this.lastAccessed = (int) System.currentTimeMillis();
            this.expiration = expire;

        }

        public Entry(int lastAccessed, long expire) {
            this.lastAccessed = lastAccessed;
            this.expiration = expire;
        }

        public long getValue() {
            return this.expiration;
        }

        public long getLastAccessed() {
            return this.lastAccessed;
        }

        public boolean isExpired() {
            return (lastAccessed + expiration) < System.currentTimeMillis();
        }

        public void updateUseTime() {
            this.lastAccessed = (int) System.currentTimeMillis();
        }

    }

    static {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        for (String key : cacheMap.keySet()) {
                            if (cacheMap.get(key).isExpired()) {
                                
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

    public static void put(String key, long expiration) {
        cacheMap.put(key, new Entry(expiration));
    }
    
    public boolean isExpired(String key){
        Entry entry = cacheMap.get(key);
        
        return entry.isExpired();
    }

    public static int get(String key) {
        Entry entry = cacheMap.get(key);
        
        if (entry != null) {
            entry.updateUseTime();
            return (int) entry.getLastAccessed();
        } else {
            return -1;
        }
    }

    public void read(File file) {
        try {
            Scanner sc = new Scanner(file);
            String webPage;
            long lastAccessed;
            while (sc.hasNextLine()) {
                webPage = sc.next();
                lastAccessed = Long.parseLong(sc.next());
                cacheMap.put(webPage, new Entry((int) lastAccessed, 100000));
            }
        } catch (FileNotFoundException ex) {
            System.out.println("file not found.");
        }
    }

}
