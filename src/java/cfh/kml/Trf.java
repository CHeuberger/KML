package cfh.kml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

public class Trf {

    private static final Pattern pattern = Pattern.compile(
            "document\\.getElementById\\(\"main\"\\)\\.parentNode\\.style\\.display=\"none\";");

    public static void main(String[] args) {
        JFileChooser ch = new JFileChooser();
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = ch.showOpenDialog(null);
        if (option != JFileChooser.APPROVE_OPTION)
            return;
        
        File dir = ch.getSelectedFile();
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".html")) {
                convert(file);
            }
        }
    }

    private static void convert(File file) {
        String name = file.getName();
        File tmp = new File(file.getParentFile(), name + ".tmp");
        System.out.println("creating " + tmp);
        boolean changed = false;
        
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
                try {
                    String line;
                    while((line = in.readLine()) != null) {
                        if (pattern.matcher(line).find()) {
                            System.out.println(line);
                            changed = true;
                        } else  {
                            out.write(line);
                        }
                        out.newLine();
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        if (changed) {
            File bak = new File(file.getParent(), name + ".bak");
            if (bak.exists()) {
                bak.delete();
            }
            file.renameTo(bak);
            tmp.renameTo(file);
        } else {
            tmp.delete();
        }
    }
}
