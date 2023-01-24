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
import java.util.Set;

import org.urts.data.Storer;
import org.urts.hash.Hasher;

class NameBasedCheck extends AbstractCheck {

    /** Affected set of classes */
    private final Set<String> mAffected;

    /** Extension for the file that stores dependencies */
    private final String mExtension;
    
    /**
     * Constructor.
     */
    public NameBasedCheck(Storer storer, Hasher hasher, String extension) {
        super(storer, hasher);
        this.mAffected = new HashSet<String>();
        this.mExtension = extension;
    }

    @Override
    public String includeAll(String fileName, String fileDir, Boolean horizontalCompare) {
        String className = removeExtension(fileName, mExtension).trim();
        if (isAffected(fileDir, className, mExtension, horizontalCompare)) {
            mAffected.add(className);
        }
        return className;
    }

    @Override
    public void includeAffectedFromPrev(Set<String> affectedClasses) {
        affectedClasses.addAll(mAffected);
    }

    @Override
    public void includeAffectedFromCurRound(Set<String> affectedClasses, String curRoundDirName) {
        for(String affected : mAffected) {
            affectedClasses.add(affected.trim() + AffectedChecker.ROUND_SEPARATOR + curRoundDirName);
        }
    }

    /**
     * Removes extension (and preceding dot if present) from the given string.
     * 
     * @param str
     *            String from which to remove [.]extension
     * @param ext
     *            Extension to remove
     * @return Original string if extension is not present, string without
     *         [.]extension otherwise
     */
    protected static String removeExtension(String str, String ext) {
        if (!str.endsWith(ext)) {
            return str;
        }
        int index = str.lastIndexOf(ext);
        index = index <= 0 ? 1 : index;
        str = str.substring(0, index - 1);
        return str;
    }
}
