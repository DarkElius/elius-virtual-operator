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

import elius.virtualoperator.VirtualOperator;
import elius.virtualoperator.VirtualOperatorID;
import elius.webapp.framework.db.DBUtils;

public class TaskExecutor implements Runnable {

	// Get logger
	private static Logger logger = LogManager.getLogger(TaskExecutor.class);
	
	// Task Database
	private TaskDatabase dbTask;
	
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
		taskManager = VirtualOperator.getInstance().getTaskManager();
	}
	
	
	@Override
	public void run() {	
		// Log task information
		logger.debug("Start - Task Id(" + task.getUuid() + ") Status(" + task.getStatus().getName() + ") Type(" + task.getType() + ") Priority(" + task.getPriority().getName() + ") Details(" + task.getDetails() + ")");

		try {

			// Update task owner with the real thread executor
			dbTask.updateOwner(task.getUuid(), VirtualOperatorID.get());
			
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
		} catch (Throwable t) {
			task.setStatus(TaskStatus.ERROR);
			task.setResult("Task failed unexpectedly, please check system logs");
			logger.error("Task failed unexpectedly", t);
		}

		// Update message
		dbTask.updateResult(task.getUuid(), task.getStatus(), task.getResult());

		// Log end task
		logger.debug("End - Task Id(" + task.getUuid() + ") Status(" + task.getStatus().getName() + ") Result(" + DBUtils.truncate(task.getResult(), 64, true) + ") Type(" + task.getType() + ") Priority(" + task.getPriority().getName() + ") Details(" + task.getDetails() + ")");

	}

}
