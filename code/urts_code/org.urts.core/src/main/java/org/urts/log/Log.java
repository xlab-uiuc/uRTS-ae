/*
 * Copyright 2014-present Milos Gligoric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.urts.log;

import org.urts.Config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Simple logging facility.
 */
public final class Log {

    private static final String DEBUG_TEXT = "Ekstazi_Debug";
    private static final String ERROR_TEXT = "Ekstazi_Error";
    private static final String WARN_TEXT = "Ekstazi_Warn";
    private static final String CONF_TEXT = "Ekstazi_Conf";

    public static final String DIFF_LOG_FOLDER = "diffLog";
    public static final String D2F_FILE_NAME = "D2FLog.txt";

    private static PrintWriter pwScreen;
    private static PrintWriter pwFile;

    private static Boolean myLogEnabled = false;
    private static Boolean diffLogEnabled = false;

    private static Boolean firstEnterDiffLog = true;


    private static Boolean AffectedLogEnabled = false;
    private static Boolean firstEnterAffectedLog = true;
    public static String  Affected_LOG_FOLDER = "AffectedLog";

//    private static Set<String> loggedConfig = new HashSet<>();
//    private static Set<String> loggedURL = new HashSet<>();

    public static void initScreen() {
        init(true, false, null);
    }

    public static void init(boolean printToScreen, boolean printToFile, String logFileName) {
        if (printToScreen) {
            pwScreen = new PrintWriter(System.err);
        }
        if (printToFile) {
            try {
                File file = new File(logFileName);
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    w("Was unable to create log file");
                    return;
                }
                pwFile = new PrintWriter(file);
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        pwFile.close();
                    }
                });
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Debugging.
     */
    public static final void d(Object... messages) {
        if (Config.DEBUG_V) {
            print(DEBUG_TEXT + ": ");
            for (int i = 0; i < messages.length; i++) {
                print(messages[i]);
                if (i != messages.length - 1) {
                    print(" ");
                }
            }
            println(".");
        }
    }

    /**
     * Debugging.
     */
    public static final void d(String msg, int val) {
        if (Config.DEBUG_V) {
            d(msg, Integer.toString(val));
        }
    }

    /**
     * Error during initialization (e.g., configuration error).
     *
     * @param msg
     *            Message to be reported.
     */
    public static final void e(String msg) {
        println(ERROR_TEXT + ": " + msg);
    }

    public static final void e(String msg, Exception ex) {
        e(msg);
        if (Config.DEBUG_V) {
            ex.printStackTrace();
        }
    }

    /**
     * Something may affect performance but not correctness.
     */
    public static final void w(String msg) {
        println(WARN_TEXT + ": " + msg);
    }

    /**
     * Printing configuration options.
     */
    public static final void c(String msg) {
        if (msg.replaceAll("\\s+", "").equals("")) {
            println(CONF_TEXT);
        } else {
            println(CONF_TEXT + ": " + msg);
        }
    }

    public static final void c(Object key, Object value) {
        if (key.equals("") && value.equals("")) {
            c("");
        } else {
            c(key + " = " + value);
        }
    }

    private static void print(Object s) {
        if (pwScreen != null) {
            pwScreen.print(s);
            pwScreen.flush();
        }
        if (pwFile != null) {
            pwFile.print(s);
            pwFile.flush();
        }
    }

    public static void println(Object s) {
        if (pwScreen != null) {
            pwScreen.println(s);
            pwScreen.flush();
        }
        if (pwFile != null) {
            pwFile.println(s);
            pwFile.flush();
        }
    }

    public static void d2f (String s) {
        if (!myLogEnabled)
            return;
        try {
            FileWriter fw = new FileWriter(D2F_FILE_NAME, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(s);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void d2f (List<String> list) {
        if (!myLogEnabled)
            return;
        try {
            FileWriter fw = new FileWriter(D2F_FILE_NAME, true);
            BufferedWriter bw = new BufferedWriter(fw);
            if (list != null && !list.isEmpty()) {
                for (String s : list) {
                    if (s != null) {
                        bw.write(s);
                        bw.newLine();
                    }
                }
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void codeDiffLog(String curFolder, String url, String dirName, String className, String msg) {
        if (!diffLogEnabled)
            return;
        try {
            String logFolderPath = Paths.get(curFolder, DIFF_LOG_FOLDER).toAbsolutePath().toString();
            if (firstEnterDiffLog) {
                File logFolder = new File(logFolderPath);
                if (!logFolder.exists()) {
                    if(!logFolder.mkdir()) {
                        throw new IOException("Can't create diffLog folder");
                    }
                } else {
                    String[] entries = logFolder.list();
                    for(String s: entries){
                        File f = new File(logFolder.getPath(), s);
                        f.delete();
                    }
                }
                firstEnterDiffLog = false;
            }
            Path logFile = Paths.get(logFolderPath, className + ".txt");
            FileWriter fw = new FileWriter(logFile.toFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("[CODE-DIFF] file= " + url + ", compared with " + dirName + " msg = " + msg);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void configDiffLog(String curFolder, String key, String depValue, String userValue, String msg, String className) {
        if (!diffLogEnabled)
            return;
        try {
            String logFolderPath = Paths.get(curFolder, DIFF_LOG_FOLDER).toAbsolutePath().toString();
            if (firstEnterDiffLog) {
                File logFolder = new File(logFolderPath);
                if (!logFolder.exists()) {
                    if(!logFolder.mkdir()) {
                        throw new IOException("Can't create diffLog folder");
                    }
                } else {
                    String[] entries = logFolder.list();
                    for(String s: entries){
                        File f = new File(logFolder.getPath(), s);
                        f.delete();
                    }
                }
                firstEnterDiffLog = false;
            }
            Path logFile = Paths.get(logFolderPath, className + ".txt");
            FileWriter fw = new FileWriter(logFile.toFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("[CONFIG-DIFF] config_name=" + key + ", dep_value=" + depValue + ", user_value=" + userValue +  ", msg=" + msg);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void AffectedLog(String curFolder, String roundIndex, List<String> affectedList, String msg) {
        if (!AffectedLogEnabled)
            return;
        try {
            String logFolderPath = Paths.get(curFolder, Affected_LOG_FOLDER).toAbsolutePath().toString();
            d2f("[DEBUG] root = " + logFolderPath);
            if (firstEnterAffectedLog) {
                File logFolder = new File(logFolderPath);
                if (!logFolder.exists()) {
                    if(!logFolder.mkdir()) {
                        throw new IOException("Can't create unaffected log folder");
                    }
                }
                firstEnterAffectedLog = false;
            }
            Path logFile = Paths.get(logFolderPath, roundIndex + ".txt");
            FileWriter fw = new FileWriter(logFile.toFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("==============================" + msg + "==============================\n");
            for (String className : affectedList) {
                bw.write(className + "\n");
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printConfig(Map<String, String> userConfig, String fileIndex) {
        try {
            FileWriter fw = new FileWriter("config_debug" + fileIndex +".txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            if (userConfig == null || userConfig.isEmpty()) {
                bw.write("Failed to print empty configuration");
                bw.newLine();
            } else {
                for (Map.Entry<String, String> entry : userConfig.entrySet()) {
                    bw.write(entry.getKey() + " , " + entry.getValue());
                    bw.newLine();
                }
            }
            bw.write("=========This is the end===========");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void trace2f () {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        new Throwable().printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        d2f(sStackTrace);
    }

    public static void prepare(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
    }

    public static void write(final String path,final byte[] bytes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    prepare(path);
                    Files.write(Paths.get(path), bytes);
                } catch (Throwable t){
                    t.printStackTrace();
                }
            }
        }).start();
    }

}
