/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author speel_000
 * @param <Key>
 * @param <Value>
 */
public class Btree<Key extends Comparable<String>, Value> {

    private static final int max = 4;
    private Node root;
    private int height;
    private int size;
    private int position = 0;
    private RandomAccessFile file;
    public boolean needToCreate;
    //private ObjectOutputStream out;
    private static final class Node {

        private int numChildren;
        private final Entry[] children = new Entry[max];

        private Node(int k) {
            numChildren = k;
        }
    }

    private static final class Entry {

        private Comparable key;
        private final HashTable value;
        private Node next;

        public Entry(Comparable key, HashTable value, Node next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    public Btree(RandomAccessFile file) {

        root = new Node(0);
        this.file = file;

        try {
            if (file.length() == 0) {
                needToCreate = true;
                createFile();
            } else 
                needToCreate = false;           //read in the nodes
     
        } catch (IOException ex) {
            System.out.println("Cannot create the Btree class.");
        }
    }

    public Btree() {
        root = new Node(0);
    }

    private void createFile() {
        try {
            position = 0;
            file.seek(position);
        } catch (IOException ex) {
            Logger.getLogger(Btree.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int size() {
        return size;
    }

    public int height() {
        return height;
    }

    private HashTable search(Node x, Key key, int ht) {
        Entry[] children = x.children;
        if (ht == 0) {
            for (int j = 0; j < x.numChildren; j++) {
                if (eq(key, children[j].key)) {
                    return children[j].value;
                }
            }
        } else {
            for (int j = 0; j < x.numChildren; j++) {
                if (j + 1 == x.numChildren || lessThan(key, children[j + 1].key)) {
                    return search(children[j].next, key, ht - 1);
                }
            }
        }
        
        return null;
    }

    public HashTable search(Key key) {
        return search(root, key, height);
    }

    

    public void put(Key key, String website, int freq) {
        HashTable h = search(root, key, height);
        
        if ((h) == null) {
            h = new HashTable();
            h.put(website, freq);
            Node u = insert(root, key, h, height);
            size++;
            if (u == null) {
                return;
            }
            Node t = new Node(2);
            t.children[0] = new Entry(root.children[0].key, null, root);
            t.children[1] = new Entry(u.children[0].key, null, u);
            root = t;
            height++;
        } else {
            if (h.containsWebsite(website)) {
                h.increment(website);
            } else {
                h.put(website, freq);
            }
        }
    }

    private Node insert(Node h, Key key, HashTable value, int ht) {
        int j;
        Entry t = new Entry(key, value, null);
        if (ht == 0) {
            for (j = 0; j < h.numChildren; j++) {
                if (lessThan(key, h.children[j].key)) {
                    break;
                }
            }
        } else {
            for (j = 0; j < h.numChildren; j++) {
                if ((j + 1 == h.numChildren) || lessThan(key, h.children[j + 1].key)) {
                    Node u = insert(h.children[j++].next, key, value, ht - 1);
                    if (u == null) {
                        return null;
                    }
                    t.key = u.children[0].key;
                    t.next = u;
                    break;
                }
            }
        }
        for (int i = h.numChildren; i > j; i--) {
            h.children[i] = h.children[i - 1];
        }
        h.children[j] = t;
        h.numChildren++;
        if (h.numChildren < max) {
            return null;
        } else {
            return split(h);
        }
    }

    private Node split(Node h) {
        Node t = new Node(max / 2);
        h.numChildren = max / 2;
        for (int j = 0; j < max / 2; j++) {
            t.children[j] = h.children[max / 2 + j];
        }
        return t;
    }

    private void write(Node node, int ht) {
        try {
            file.seek(position);
            Entry[] children = node.children;

            if (ht == 0) {
                for (int j = 0; j < node.numChildren; j++) {
                    String k = (String) children[j].key;
                    byte[] kbytes = k.getBytes();
                    file.write(kbytes);
                    int keyPad = 50 - kbytes.length;
                    byte[] keyPadB = new byte[keyPad];
                    for (int i = 0; i < keyPad; i++) {
                        keyPadB[i] = ' ';
                    }
                    file.write(keyPadB);
                    position += 30;
                    file.seek(position);
                    serialize(children[j].value);

                }
            } else {
                for (int j = 0; j < node.numChildren; j++) {
                    write(children[j].next, ht - 1);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Btree.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void write() {
        write(root, height);
    }

    
    public void readFile(Cache cacheMap, Btree bt, WebParser parser) {
        
        String key;
        int keyLength = 30;
        int offset = 0; //offset of CURRENT file pointer (initialPos)
        byte[] kByte;
        try {

            position = 0;
            file.seek(position);

            while (position < file.length()) {

                kByte = new byte[30];
                key = "";

                file.read(kByte, offset, keyLength);
                position += keyLength;

                for (int i = 0; i < kByte.length; i++) {
                    char c = (char) kByte[i];
                    if (c != ' ')
                        key += c;
                }
                
                HashTable hTemp = deserialize();
                if (hTemp != null) {
                    for (int i = 0; i < hTemp.size(); i++) {
                        if (hTemp.get(i) != null) {
                            String website = hTemp.getWebsite(i);
                            int frequency = hTemp.getFrequency(i);
                            
                            bt.put(key.toLowerCase(), website, frequency);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Btree.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

   

    private void serialize(HashTable table) {
        //serialize to byte array
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(table);
            out.close();

            //get the bytes of the serialize object
            byte[] buf = bos.toByteArray();

            //save the position and length of the object
            file.writeInt(position + 8);

            position += 4;

            file.seek(position);
            file.writeInt(buf.length);

            position += 4;

            //write to the file
            file.seek(position);
            file.write(buf);

            position += buf.length;
            //System.out.println("Adding hashTable: " + initialPos );

//            out.close();
//            fileOut.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HashTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HashTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashTable deserialize() {
        HashTable temp = null;
        try {
            //System.out.println("deserializing");
            int pos;
            int length;

            pos = file.readInt();
            position += 4;
            file.seek(position);

            length = file.readInt();
            position += 4;
            //file.seek(position);

            byte[] buf = new byte[length];
            file.seek(pos);
            file.readFully(buf);
            ByteArrayInputStream bis = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(bis);
            temp = (HashTable) ois.readObject();
            position += length;
            ois.close();

        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Btree.class.getName()).log(Level.SEVERE, null, ex);
        }
        return temp;
    }

    private boolean lessThan(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean eq(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }
}
