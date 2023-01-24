package org.urts.maven;

import org.urts.Config;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MojoLog {

    public static final String MOJO_LOG_FILE = "mojo_debug.txt";
    public static final String TIMER_FILE = "configaware_urts_analysis_time.txt";
    private static Boolean mojoLogEnabled = false;
    private static Boolean NONAffectedLogEnabled = Config.LOG_NUMBER_V;
    private static Boolean timerLogEnabled = true;
    private static Boolean firstEnterUnAffectedLog = true;
    public static String NON_Affected_LOG_FOLDER = "NonAffectedLog";


    public static void timerLog(String curRound, long timeElapsed) {
        if (!timerLogEnabled) {
            return;
        }
        try {
            FileWriter fw = new FileWriter(TIMER_FILE, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(curRound + " : " + timeElapsed + " ms");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void d2f (String s) {
        if (!mojoLogEnabled)
            return;
        try {
            FileWriter fw = new FileWriter(MOJO_LOG_FILE, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(s);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void d2f (List<String> list) {
        if (!mojoLogEnabled)
            return;
        try {
            FileWriter fw = new FileWriter(MOJO_LOG_FILE, true);
            BufferedWriter bw = new BufferedWriter(fw);
            for (String s : list) {
                bw.write(s);
                bw.newLine();
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unAffectedLog(String curFolder, String roundIndex, List<String> nonAffectedClassesFromPrev, List<String> nonAffectedClassesFromCurRound) {
        if (!NONAffectedLogEnabled)
            return;
        try {
            String logFolderPath = Paths.get(curFolder, NON_Affected_LOG_FOLDER).toAbsolutePath().toString();
            d2f("[DEBUG] MOJO unAffectedLog = " + logFolderPath);
            if (firstEnterUnAffectedLog) {
                File logFolder = new File(logFolderPath);
                if (!logFolder.exists()) {
                    if(!logFolder.mkdir()) {
                        throw new IOException("Can't create unaffected log folder");
                    }
                }
                firstEnterUnAffectedLog = false;
            }
            Path logFile = Paths.get(logFolderPath, roundIndex + ".txt");
            FileWriter fw = new FileWriter(logFile.toFile(), false);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("==============================UNAFFECTED FROM PREV==============================\n");
            for (String prev : nonAffectedClassesFromPrev) {
                bw.write(prev + "\n");
            }
            bw.write("==============================UNAFFECTED FROM CURR==============================\n");
            for (String cur : nonAffectedClassesFromCurRound) {
                bw.write(cur + "\n");
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
