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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import elius.virtualoperator.VirtualOperatorId;
import elius.webapp.framework.application.ApplicationContext;
import elius.webapp.framework.db.DbUtils;

/**
 * Asynchronous worker task responsible for executing individual task items 
 * inside separate threads managed by the executor service.
 * 
 * <p>This class handles runtime state updates, dynamic plugin resolution via the 
 * task manager, execution safety checks with robust exception handling, and persistence 
 * of final outcomes back to the database.
 * 
 * @see TaskSubmitter
 * @see TaskProcess
 */
public class TaskExecutor implements Runnable {

	// Get logger
	private static Logger logger = LogManager.getLogger(TaskExecutor.class);
	
	// Task Database
	private final TaskDatabase dbTask;
	
	// Task pointer
	Task task;
	
	// Task Manger
	TaskManager taskManager;
	
	/**
	 * Constructor
	 * @param t Task to be executed
	 */
	public TaskExecutor(Task t) {
		// Set task
		this.task = t;
		
		// Task Database instance
		dbTask = new TaskDatabase();

		// Task Manager
		taskManager = ApplicationContext.get(TaskManager.class);
	}
	
	
	/**	(non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {	
		// Log task information
		logger.trace("task={}", task.getUuid());

		try {

			// Update task owner with the real thread executor
			dbTask.updateOwner(task.getUuid(), VirtualOperatorId.get());
			
			// Get Task Process for the specified task		
			TaskProcess taskProcess = (TaskProcess)taskManager.findPlugin(task.getType());

			if(null != taskProcess) {

				// Update task status
				dbTask.updateStatus(task.getUuid(), TaskStatus.RUNNING);

				// Execute task
				taskProcess.execute(task);

				// Update task info for log
				task.setStatus(taskProcess.getTaskStatusResult());
				task.setResult(taskProcess.getProcessResult());

			} else {
				
				// Unable to process task
				task.setStatus(TaskStatus.ERROR);
				task.setResult("No Task Process found");

			}
		} catch (Exception e) {
			task.setStatus(TaskStatus.ERROR);
			task.setResult("Task failed unexpectedly, please check system logs");
			logger.error("Task failed unexpectedly", e);
		}

		// Update message
		dbTask.updateResult(task.getUuid(), task.getStatus(), task.getResult());

		// Log end task
		if(logger.isDebugEnabled()) {
			logger.debug("End - task={} result={}", task, DbUtils.truncate(task.getResult(), 64, true));
		}

	}

}
