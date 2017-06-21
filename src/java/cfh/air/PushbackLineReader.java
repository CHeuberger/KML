package cfh.air;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

public class PushbackLineReader {

    private final LineNumberReader reader;
    
    private String actualLine = null;
    
    public PushbackLineReader(LineNumberReader reader) {
        this.reader = reader;
    }
    
    public PushbackLineReader(Reader reader) {
        this(new LineNumberReader(reader));
    }
    
    public synchronized String readLine() throws IOException {
        if (actualLine == null)
            return reader.readLine();
        
        String tmp = actualLine;
        actualLine = null;
        return tmp;
    }
    
    public synchronized void pushback(String line) {
        if (actualLine != null)
            throw new IllegalStateException("only one pushback allowed");
        actualLine = line;
    }
    
    public int getLineNumber() {
        return reader.getLineNumber();
    }

    public void close() throws IOException {
        reader.close();
    }
}
