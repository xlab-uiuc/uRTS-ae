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

package org.urts.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.urts.Config;
import org.urts.log.Log;
import org.urts.util.FileUtil;

/**
 * Storing dependencies in text format. This class stores mapping
 * URL(external form)->hash. There is one line for each URL. URL and
 * hash are separated by ' _ ' character.
 */
public class TxtStorer extends Storer {

    /** State passed when processing each line */
    protected static class State {
    }

    /** Character between path and hash */
    protected static final String SEPARATOR = " _ ";

    /** Character between Configuration Name and Value */
    protected static final String CONFIG_SEPARATOR = " = ";

    /** Length of the separator (to avoid invoking length() many times) */
    protected static final int SEPARATOR_LEN = SEPARATOR.length();

    /** Length of the Config_separator (to avoid invoking length() many times) */
    protected static final int CONFIG_SEPARATOR_LEN = CONFIG_SEPARATOR.length();

    /** Indicates that magic sequence should be checked */
    private final boolean mCheckMagicSequence;

    /**
     * Constructor.
     */
    public TxtStorer() {
        this(Mode.TXT, true);
    }

    /**
     * Constructor.
     */
    public TxtStorer(Mode mode) {
        this(mode, true);
    }

    /**
     * Constructor.
     */
    public TxtStorer(boolean checkMagicSequence) {
        this(Mode.TXT, checkMagicSequence);
    }

    /**
     * Constructor.
     */
    public TxtStorer(Mode mode, boolean checkMagicSequence) {
        super(mode);
        this.mCheckMagicSequence = checkMagicSequence;
    }

    // LOAD
    @Override
    protected final Map<String, String> extendedLoadConfigMap(FileInputStream fis) {
        Map<String, String> configMap = new HashMap<String, String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(createReader(fis));
            // Check magic sequence.
            if (isMagicCorrect(br)) {
                // If magic is correct, load data.
                Boolean isConfig = false;
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.equals("Configuration_Name = Configuration_Value")) {
                        isConfig = true;
                        continue;
                    }
                    if (isConfig) {
                        int sepIndex = line.indexOf(CONFIG_SEPARATOR);
                        String key = line.substring(0, sepIndex);
                        String value = line.substring(sepIndex + CONFIG_SEPARATOR_LEN);
                        configMap.put(key, value);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("Loading coverage not successful", ex);
            // Make sure that test is rerun.
            configMap.clear();
        } finally {
            FileUtil.closeAndIgnoreExceptions(br);
        }
        return configMap;
    }

    @Override
    protected final Set<RegData> extendedLoadRegData(FileInputStream fis) {
        Set<RegData> regData = new HashSet<RegData>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(createReader(fis));
            // Check magic sequence.
            if (isMagicCorrect(br)) {
                // If magic is correct, load data.
                State state = newState();
                while (true) {
                    String line = br.readLine();        // Shuai: Each line: URL _ ChecksumValues
                    if (line == null || line.equals("Configuration_Name = Configuration_Value")) {
                        break;
                    }
                    RegData regDatum = parseLine(state, line);
                    // regDatum can be null if one line does not correspond to
                    // one resource.
                    if (regDatum != null) {
                        regData.add(regDatum);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("Loading coverage not successful", ex);
            // Make sure that test is rerun.
            regData.clear();
        } finally {
            FileUtil.closeAndIgnoreExceptions(br);
        }
        return regData;
    }

    protected Reader createReader(FileInputStream fis) {
        return new InputStreamReader(fis);
    }

    /**
     * Check magic sequence. Note that subclasses are responsible to decide if
     * something should be read from buffer. This approach was taken to support
     * old format without magic sequence.
     */
    protected boolean isMagicCorrect(BufferedReader br) throws IOException {
        if (mCheckMagicSequence) {
            String magicLine = br.readLine();
            return (magicLine != null && magicLine.equals(mMode.getMagicSequence()));
        } else {
            return true;
        }
    }

    /**
     * Parses a single line from the file. This method is never invoked for
     * magic sequence. The line includes path to file and hash. Subclasses may
     * include more fields.
     *
     * This method returns null if the line is of no interest. This can be used
     * by subclasses to implement different protocols.
     */
    protected RegData parseLine(State state, String line) {
        // Note. Initial this method was using String.split, but that method
        // seems to be much more expensive than playing with indexOf and
        // substring.
        int sepIndex = line.indexOf(SEPARATOR);
        String urlExternalForm = line.substring(0, sepIndex);
        String hash = line.substring(sepIndex + SEPARATOR_LEN);
        return new RegData(urlExternalForm, hash);
    }

    // STORE

    @Override
    protected final void extendedSave(FileOutputStream fos, Set<RegData> hashes, Map<String, String> configMap) {
        Writer pw = new BufferedWriter(createWriter(fos));
        try {
            // Print magic sequence (print separate to avoid new Strings).
            pw.write(mMode.getMagicSequence());
            pw.write('\n');
            State state = newState();
            // Sort is not necessary, but good for debugging; we assume that set
            // is sorted.
            // Print hashes (we print each separate to avoid new Strings).
            for (RegData regDatum : hashes) {
                printLine(state, pw, regDatum.getURLExternalForm(), regDatum.getHash());
                // printLine(state, pw, "# Hello", "World!");
                // throw new RuntimeException();
            }
            printConfigLine(pw, "Configuration_Name", "Configuration_Value");
            for (Map.Entry<String, String> configEntry : configMap.entrySet()) {
                String configName = configEntry.getKey();
                // If there is no config.excludes declared by user, directly add config to dep data
                if (Config.CONFIG_EXCLUDES_V == null || Config.CONFIG_EXCLUDES_V.isEmpty()) {
                    printConfigLine(pw, configName, configEntry.getValue());
                } else if (!Config.CONFIG_EXCLUDES_V.contains(configName)) {
                    printConfigLine(pw, configName, configEntry.getValue());
                }
            }
        } catch (IOException ex) {
            Log.e("Problems while saving dependencies");
        } finally {
            FileUtil.closeAndIgnoreExceptions(pw);
        }
    }

    protected Writer createWriter(FileOutputStream fos) {
        return new OutputStreamWriter(fos);
    }

    /**
     * Prints one line to the given writer; the line includes path to file and
     * hash. Subclasses may include more fields.
     */
    protected void printLine(State state, Writer pw, String externalForm, String hash) throws IOException {
        pw.write(externalForm);
        pw.write(SEPARATOR);
        pw.write(hash);
        pw.write('\n');
    }

    protected void printConfigLine(Writer pw, String configName, String configValue) throws IOException {
        pw.write(configName);
        pw.write(CONFIG_SEPARATOR);
        pw.write(configValue);
        pw.write('\n');
    }

    // STATE

    /**
     * Creates a new state that will be passed for each line being processed.
     */
    protected State newState() {
        return null;
    }

    // MAIN (and "tests")

    public static void main(String[] args) {
        // Small test to measure time for parseLine.
        // long maxIter = Long.parseLong(args[0]);
        // TxtStorer storer = new TxtStorer();
        // for (int i = 0; i < maxIter; i++) {
        // storer.parseLine("some text" + SEPARATOR + "444444");
        // }
    }

    // Initial silly test.
    public static void testWriter(long maxIter) throws IOException {
        TxtStorer storer = new TxtStorer();
        // Small test to measure time to write to file.
        Set<RegData> hashes = new HashSet<RegData>();
        Map<String, String> configMap = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            hashes.add(new RegData("a", i + "*"));
        }
        for (int i = 0; i < 1000; i++) {
            configMap.put(i + "th_Config", i + "th_Value");
        }
        long begin = System.currentTimeMillis();
        for (long i = 0; i < maxIter; i++) {
            FileOutputStream fos = new FileOutputStream("AAAA");
            storer.extendedSave(fos, hashes, configMap);
            fos.close();
        }
        System.out.println(System.currentTimeMillis() - begin);
    }
}
