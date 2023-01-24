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

package org.urts.maven;

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

import org.urts.log.Log;
import org.urts.util.FileUtil;
import org.urts.Names;

/**
 * Removes Ekstazi directories.
 */
@Mojo(name = "clean", defaultPhase = LifecyclePhase.CLEAN)
public class CleanEkstaziMojo extends AbstractEkstaziMojo {

    public void execute() throws MojoExecutionException {
        File fileList[] = parentdir.listFiles();
        for (File f : fileList) {
            if (f.getName().contains(Log.D2F_FILE_NAME)) {
                f.delete();
            }
            if (f.getName().contains(MojoLog.MOJO_LOG_FILE)) {
                f.delete();
            }
            if (f.isDirectory() && f.getName().contains(MojoLog.NON_Affected_LOG_FOLDER)) {
                FileUtil.deleteDirectory(f);
            }
            if (f.isDirectory() && f.getName().contains(Log.DIFF_LOG_FOLDER)) {
                FileUtil.deleteDirectory(f);
            }
            if (f.isDirectory() && f.getName().contains(Log.Affected_LOG_FOLDER)) {
                FileUtil.deleteDirectory(f);
            }
            if (f.isDirectory() && f.getName().contains(Names.EKSTAZI_ROOT_DIR_NAME)) {
                FileUtil.deleteDirectory(f);
            }
            if (f.isDirectory() && f.getName().contains(Names.EKSTAZI_CONFIGLOG_DIR_NAME)) {
                FileUtil.deleteDirectory(f);
            }
        }
    }
}
