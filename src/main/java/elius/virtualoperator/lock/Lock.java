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

package elius.virtualoperator.lock;

import java.sql.Timestamp;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * This class describe the lock object
 */
public class Lock {
	
	// Application name
	private String application;

	// Owner name
	private String owner;
	
	// Id
	private UUID uuid;
	
	// Acquired time stamp, when the lock is inserted
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Timestamp acquired;
	
	// Expire time stamp, when the lock is considered old
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Timestamp expire;

	
	/**
	 * Constructor
	 */
	public Lock() {
		
		// Initialize application name
		application = "";
		
		// Initialize owner name
		owner = "";
		
		// Generate an Id
		uuid = UUID.randomUUID(); 
		
		// Initialize acquired time stamp
		acquired = new Timestamp(0);
		
		// Initialize expire time stamp
		expire = new Timestamp(0);
	}

	
	/**
	 * Get application name
	 * @return Return application name
	 */
	public String getApplication() {
		return application;
	}

	
	/**
	 * Set application name
	 * @param application Application Name
	 */
	public void setApplication(String application) {
		this.application = application;
	}
	

	/**
	 * Get owner name
	 * @return Owner name
	 */
	public String getOwner() {
		return owner;
	}

	
	/**
	 * Set owner name
	 * @param owner Owner name
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	
	/**
	 * Get Id
	 * @return Id
	 */
	public UUID getUuid() {
		return uuid;
	}

	
	/**
	 * Set Id
	 * @param uuid Id
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	
	/**
	 * Get acquired time stamp
	 * @return Acquired time stamp
	 */
	public Timestamp getAcquired() {
		return acquired;
	}
	
	
	/**
	 * Set acquired time stamp
	 * @param acquired time stamp
	 */
	public void setAcquired(Timestamp acquired) {
		this.acquired = acquired;
	}

	
	/**
	 * Get expire time stamp
	 * @return expire time stamp
	 */
	public Timestamp getExpire() {
		return expire;
	}

	
	/**
	 * Set expire time stamp
	 * @param expire time stamp
	 */
	public void setExpire(Timestamp expire) {
		this.expire = expire;
	}


}
