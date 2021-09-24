package eu.internetpolice.slapcord;

import java.io.File;

public class Bootstrap {
    public static void main(String[] args) {
        String path = new File(".").getAbsolutePath();
        if (path.contains("!") || path.contains("+")) {
            System.err.println("Cannot run server in a directory with ! or + in the pathname. Please rename the affected folders and try again.");
            return;
        }

        float javaVersion = Float.parseFloat(System.getProperty("java.class.version"));
        if (javaVersion < 60.0) {
            System.err.println("Unsupported Java detected (" + javaVersion + "). Required is min. Java 16.");
            return;
        }

        new Bot(args);
    }
}
