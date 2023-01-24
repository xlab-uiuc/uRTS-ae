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

package org.urts.check;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.urts.Config;
import org.urts.configAware.ConfigLoader;
import org.urts.configAware.ConfigMapping;
import org.urts.data.RegData;
import org.urts.data.Storer;
import org.urts.hash.Hasher;
import org.urts.log.Log;

abstract class AbstractCheck {

    /** Storer */
    protected final Storer mStorer;

    /** Hasher */
    protected final Hasher mHasher;

    /** Flag to indicate configuration's value is got from default-reset */
    private final String configDefaultFlag = "@DEFAULTVALUE4CONFIGAWARE@";

    /** Flag to separator configuration's value that got from several APIs */
    private final String configValueSeparator = "@CONFIGAWARESEPARATOR@";

    /**
     * Constructor.
     */
    public AbstractCheck(Storer storer, Hasher hasher) {
        this.mStorer = storer;
        this.mHasher = hasher;
    }

    public abstract String includeAll(String fileName, String fileDir, Boolean horizontalCompare);

    public abstract void includeAffectedFromPrev(Set<String> affectedClasses);

    public abstract void includeAffectedFromCurRound(Set<String> affectedClasses, String curRoundDirName);

    protected boolean isAffected(String dirName, String className, String methodName, Boolean horizontalCompare) {
        if (horizontalCompare) {
            return isAffectedByConfig(mStorer.loadConfigData(dirName, className, methodName), dirName, className);
        } else {
            return isAffectedByReg(mStorer.loadRegData(dirName, className, methodName), dirName, className)
                    || isAffectedByConfig(mStorer.loadConfigData(dirName, className, methodName), dirName, className);
        }
    }

    protected boolean isAffectedByReg(Set<RegData> regData, String dirName, String className) {
        return regData == null || regData.size() == 0 || hasHashChanged(regData, dirName, className);
    }

    protected boolean isAffectedByConfig(Map<String, String> configMap, String dirName, String className) {
        return configMap != null && !configMap.isEmpty() && hasConfigChanged(configMap, dirName, className);
    }

    /**
     * Check if any element of the given set has changed.
     */
    private boolean hasHashChanged(Set<RegData> regData, String dirName, String className) {
        for (RegData el : regData) {
            if (hasHashChanged(mHasher, el)) {
                Log.codeDiffLog(Config.curWorkingDir(), el.getURLExternalForm(), dirName, className, " Code diff in AbstractChecker");
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the given datum has changed using the given hasher.
     */
    protected final boolean hasHashChanged(Hasher hasher, RegData regDatum) {
        String urlExternalForm = regDatum.getURLExternalForm();
        // Check hash.
        String newHash = hasher.hashURL(urlExternalForm);
        boolean anyDiff = !newHash.equals(regDatum.getHash());
        return anyDiff;
    }

    /**
     * Check if the configuration has changed.
     *
     */
    private boolean hasConfigChanged(Map<String, String> configMap, String dirName, String className) {
        Map<String, String> ChangedConfig = ConfigLoader.getChangedConfigMap();
        if (ChangedConfig == null) {
            Log.configDiffLog(Config.curWorkingDir(), "", "", "", "Failed to get user configuration", className);
            //Log.d2f("[ERROR] hasConfigChanged(): Failed to get user configuration");
            return true;
        }

        // For evaluation
        Boolean diff = false;
        // For every configuration pairs, compare with user's setting.
//        for(Map.Entry<String, String> entry : configMap.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue();
//
//            // (1) If user doesn't set, return true;
//            if (!ChangedConfig.containsKey(key)) {
//                Log.configDiffLog(key, value, "", "User didn't set this config", className);
//                diff = true;
//            }
//
//            // (2) If user's setting is different, return true;
//            if (!ChangedConfig.get(key).equals(value)) {
//                Log.configDiffLog(key, value, ChangedConfig.get(key), "Value different!", className);
//                Log.d2f("Diff!! Key = " + key + " value = " + value + " / " + ChangedConfig.get(key));
//                diff = true;
//            }
//        }

        String testNames [] = className.split("\\.");
        String testName = testNames[testNames.length - 1];
        Set<String> CtestMappingConfigs = new HashSet<>();
        if (ConfigMapping.getOnlyTestNameMapping().containsKey(testName)) {
            //Log.d2f("[INFO] " + testName + " Mapping load successfully for comparison");
            CtestMappingConfigs = ConfigMapping.getOnlyTestNameMapping().get(testName);
        }
        for (Map.Entry<String, String> entry : ChangedConfig.entrySet()) {
            String key = entry.getKey();
            String [] userValues = entry.getValue().split(configValueSeparator);
            // (1) If this config is not used by test, continue
            if (!configMap.containsKey(key)) {
                continue;
            }

            // (2) If this config is in ctest mapping (can't be tested under current test), continue
            if (CtestMappingConfigs.contains(key)) {
                //Log.d2f("[INFO] " + key + " is skipped for " + testName + " comparison due to ctest mapping");
                continue;
            }

            String depValue = configMap.get(key);

            // (3) If user's setting is different, return true
            Boolean atLeastOneValueSame = false;
            for (String userValue : userValues) {
                if (!depValue.equals(userValue)) {
                    if (depValue.contains(configDefaultFlag)) {
                        if (userValue.equals("null")) {
                            atLeastOneValueSame = true;
                        } else if (userValue.equals(depValue.replace(configDefaultFlag, ""))) {
                            atLeastOneValueSame = true;
                        }
                    }
                } else {
                    atLeastOneValueSame = true;
                }
            }
            if (!atLeastOneValueSame) {
                Log.configDiffLog(Config.curWorkingDir(), key, configMap.get(key), entry.getValue(), "Value different in AbstractCheck! Compared with " + dirName, className);
                diff = true;
            }
        }

        if (diff) {
            return true;
        }
        // (4) else return false;
        return false;
    }
}
