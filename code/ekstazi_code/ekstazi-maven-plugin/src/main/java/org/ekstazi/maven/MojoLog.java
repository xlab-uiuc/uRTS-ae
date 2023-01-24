package org.ekstazi.maven;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class MojoLog {
    public static final String TIMER_FILE = "configaware_ekstazi_analysis_time.txt";
    private static Boolean timerLogEnabled = true;

    public static void timerLog(String msg, long timeElapsed) {
        if (!timerLogEnabled) {
            return;
        }
        try {
            FileWriter fw = new FileWriter(TIMER_FILE, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write( "Times : " + timeElapsed + " ms, msg = " + msg);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}