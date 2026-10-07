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

package elius.virtualoperator;

import java.lang.management.ManagementFactory;

public class VirtualOperatorID {

	/**
	 * Get the application name that identify the current node, process, thread in the system
	 * @return Application identifier
	 */
	public static String get() {
		
		// Returns the name representing the running Java virtual machine + thread name + thread ID
		return ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + " - " + Thread.currentThread().threadId();
		
	}
	
}
