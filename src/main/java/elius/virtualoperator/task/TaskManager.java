/*
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

package elius.virtualoperator.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import elius.webapp.framework.plugin.Plugin;
import elius.webapp.framework.plugin.PluginManager;

/**
 * Application-specific plugin manager.
 *
 * <p>This class extends the generic {@link PluginManager} and provides
 * access to plugins implementing the {@link TaskProcess} interface.</p>
 */
public class TaskManager extends PluginManager {

	/**
	 * Default constructor
	 */
	public TaskManager() {
	}

    /**
     * Returns all loaded task processing plugins.
     *
     * <p>Only plugins implementing {@link TaskProcess} are included
     * in the returned collection.</p>
     *
     * <p>The returned collection is read-only.</p>
     *
     * @return collection of loaded task processing plugins
     */
    public Collection<TaskProcess> getApplicationPlugins() {

        List<TaskProcess> result = new ArrayList<>();

        for (Plugin plugin : getLoadedPlugins()) {

            if (plugin instanceof TaskProcess) {
                result.add((TaskProcess) plugin);
            }
        }

        return Collections.unmodifiableCollection(result);
    }
}
