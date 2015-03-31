/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author speel_000
 */


public class HashTable implements Serializable{

    private final HashEntry[] websites;
    private final int initialCapacity = 500;
    

    public HashTable() {
        
        websites = new HashEntry[initialCapacity];

        for (int i = 0; i < initialCapacity; i++) {
            websites[i] = null;
        }
    }

    public HashTable(int size) {
     
        websites = new HashEntry[size];
        for (int i = 0; i < size; i++) {
            websites[i] = null;
        }
    }
    
    public void put(String website, int frequency) {
        int hash = hash(website);
        int index = abs (hash % initialCapacity);
        
        while(websites[index] != null && websites[index].getKey().equals(website)){
            index = abs((index + 1 ) % initialCapacity);
        }
        
        websites[index] = new HashEntry(website, frequency);
    }
   
    public HashEntry get(int index){
        return websites[index];
    }
    
    public String getWebsite(int index){
        return websites[index].getKey();
    }
    
    public int getFrequency(int index){
        return websites[index].getFreq();
    }

    public void increment(String website) {
        int hash = hash(website);
        int index = abs(hash % initialCapacity);
        while (websites[index] != null && !websites[index].getKey().equals(website)) {
            index = abs((index + 1) % initialCapacity);
        }

        if (websites[index].getKey().equals(website)) {
            websites[index].increment();
        }
    }
    
    public boolean containsWebsite(String website) {
        boolean contains = false;
        int hash = hash(website);
        int index = abs(hash % initialCapacity);
        
        while(websites[index] != null && !websites[index].getKey().equals(website)){
            index = abs((index + 1) % initialCapacity);
        }
        
        if(websites[index] == null)
            return contains;
        else if (websites[index].getKey().equals(website))
            contains = true;
        
        return contains;
    }

    private int hash(String s) {
        int hash = 0;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update((s.getBytes()));
            String encryptedString = new String(messageDigest.digest());
            
            hash = ((s.hashCode() ^ encryptedString.hashCode()) % websites.length);

            if (hash < 0) {
                hash = abs(hash);
            }

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(HashTable.class.getName()).log(Level.SEVERE, "No such algorithm exception", ex);
        }
        return hash;
    }

    private int abs(int x) {
        int y = x >> 31;
        return (x ^ y) - y;
    }

    public int size() {
        return websites.length;
    }
    


    @Override
    public String toString(){
        String s = "";
        for(HashEntry entry: websites){
            if(entry != null){
                s += "WebPage: " + entry.getKey() + "---" + entry.getFreq() + "\n";
                System.out.println("WebPage: ");
                System.out.println(entry.getKey());
            }
        }
        return s;
    }
}
