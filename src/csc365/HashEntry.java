/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365;

import java.io.Serializable;

/**
 *
 * @author speel_000
 */
public class HashEntry implements Serializable{

    private final String key;
    private int freq;

    public HashEntry(String key, int freq) {
        this.key = key;
        this.freq = freq;
    }

    public String getKey() {
        return key;
    }

    public int getFreq() {
        return freq;
    }

    public void increment() {
        this.freq++;
    }
    

}
