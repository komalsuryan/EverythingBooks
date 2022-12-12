package org.amishaandkomal.utilities;

import javax.swing.filechooser.FileFilter;
import java.io.File;

class Utils {

    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String png = "png";

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
}

public class ImageFilter extends FileFilter {

    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = Utils.getExtension(f);
        if (extension != null) {
            return extension.equals(Utils.jpeg) || extension.equals(Utils.jpg) || extension.equals(Utils.png);
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "Just Images";
    }
}