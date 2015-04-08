package csc365;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 *
 * @author speel_000
 */
public class Loader {
    private final WebParser parser = new WebParser();
    private final ArrayList<String> webNameList = new ArrayList<>();
    private final RandomAccessFile aFile;
    private final FileChannel inChannel;
    private MappedByteBuffer buf;

    public Loader(String fileName) throws FileNotFoundException {

        this.aFile = new RandomAccessFile(fileName, "r");
        this.inChannel = aFile.getChannel();

    }

    public void load() throws IOException {
        
        
        buf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        
        
        CharBuffer cb = Charset.defaultCharset().decode(buf);
        String webName = cb.toString();
        String[] webNameArray = webName.split("\n");
        for(String s: webNameArray){
            webNameList.add(s);
           
        }
        
        inChannel.close();
        aFile.close();
        
        parser.parse(webNameList);
    }
    
    public WebParser getParser(){
        return parser;
    }
}
