/*
 * Copyright 2015-present Milos Gligoric
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

import java.util.Set;

import org.urts.data.RegData;
import org.urts.data.Storer;
import org.urts.hash.Hasher;

import org.urts.log.Log;

/**
 * This class is only used in debug mode.  The class should not change
 * behavior of the superclass but only print debug info of interest.
 */
final class DebugNameCheck extends NameBasedCheck {

    /**
     * Constructor.
     */
    public DebugNameCheck(Storer storer, Hasher hasher, String extension) {
        super(storer, hasher, extension);
    }

    @Override
    protected boolean isAffected(String dirName, String className, String methodName, Boolean horizontalCompare) {
        Log.d("Checking::Class::", className);
        return super.isAffected(dirName, className, methodName, horizontalCompare);
    }

    @Override
    protected boolean isAffectedByReg(Set<RegData> regData, String dirName, String className) {
        for (RegData el: regData) {
            if (hasHashChanged(mHasher, el)) {
                Log.d("Checking::Diff::", el.getURLExternalForm());
            } else {
                Log.d("Checking::Same::", el.getURLExternalForm());
            }
        }
        return super.isAffectedByReg(regData, dirName, className);
    }
}
