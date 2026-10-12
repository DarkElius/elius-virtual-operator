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

/**
 * Utility class for generating unique string identifiers for runtime nodes, 
 * processes, and application threads.
 */
public class VirtualOperatorId {

	/**
	 * Default constructor
	 */
	public VirtualOperatorId() {
	}    

    /**
     * Generates a unique identifier string combining the JVM runtime name, 
     * current thread name, and current thread ID.
     * 
     * @return a formatted string representing the current thread identifier within the system
     */
    public static String get() {
        
        // Returns a composite name representing the running Java virtual machine, thread name, and thread ID
        return ManagementFactory.getRuntimeMXBean().getName() 
                + " - " + Thread.currentThread().getName() 
                + " - " + Thread.currentThread().threadId();
        
    }
}
