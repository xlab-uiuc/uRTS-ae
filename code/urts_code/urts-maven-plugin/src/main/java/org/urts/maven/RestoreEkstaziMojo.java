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

import org.apache.maven.model.Plugin;

@Mojo(name = "restore", defaultPhase = LifecyclePhase.TEST)
public class RestoreEkstaziMojo extends AbstractEkstaziMojo {

    public void execute() throws MojoExecutionException {
        if (getSkipme()) {
            getLog().info("Ekstazi is skipped.");
            return;
        }
        if (getSkipTests()) {
            getLog().info("Tests are skipped.");
            return;
        }

        Plugin plugin = lookupPlugin(SUREFIRE_PLUGIN_KEY);
        restoreExcludesFile(plugin);
    }
}
