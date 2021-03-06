/**
 * Copyright 2016 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.conductor.client.http;

import com.google.common.base.Preconditions;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author visingh
 * @author Viren
 * Client for conductor task management including polling for task, updating task status etc.
 */
public class TaskClient extends ClientBase {

    private static GenericType<List<Task>> taskList = new GenericType<List<Task>>() {
    };

    private static GenericType<List<TaskDef>> taskDefList = new GenericType<List<TaskDef>>() {
    };

    /**
     * Creates a default task client
     */
    public TaskClient() {
        super();
    }

    /**
     * @param config REST Client configuration
     */
    public TaskClient(ClientConfig config) {
        super(config);
    }

    /**
     * @param config  REST Client configuration
     * @param handler Jersey client handler. Useful when plugging in various http client interaction modules (e.g. ribbon)
     */
    public TaskClient(ClientConfig config, ClientHandler handler) {
        super(config, handler);
    }

    /**
     * @param config  config REST Client configuration
     * @param handler handler Jersey client handler. Useful when plugging in various http client interaction modules (e.g. ribbon)
     * @param filters Chain of client side filters to be applied per request
     */
    public TaskClient(ClientConfig config, ClientHandler handler, ClientFilter... filters) {
        super(config, handler);
        for (ClientFilter filter : filters) {
            super.client.addFilter(filter);
        }
    }

    /**
     * @deprecated This API is deprecated and will be removed in the next version
     * use {@link #batchPollTasksByTaskType(String, String, int, int)} instead
     */
    @Deprecated
    public List<Task> poll(String taskType, String workerId, int count, int timeoutInMillisecond) {
        return batchPollTasksByTaskType(taskType, workerId, count, timeoutInMillisecond);
    }

    /**
     * Perform a batch poll for tasks by task type. Batch size is configurable by count.
     *
     * @param taskType             Type of task to poll for
     * @param workerId             Name of the client worker. Used for logging.
     * @param count                Maximum number of tasks to be returned. Actual number of tasks returned can be less than this number.
     * @param timeoutInMillisecond Long poll wait timeout.
     * @return List of tasks awaiting to be executed.
     */
    public List<Task> batchPollTasksByTaskType(String taskType, String workerId, int count, int timeoutInMillisecond) {
        Preconditions.checkArgument(StringUtils.isNotBlank(taskType), "Task type cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(workerId), "Worker id cannot be blank");
        Preconditions.checkArgument(count > 0, "Count must be greater than 0");

        Object[] params = new Object[]{"workerid", workerId, "count", count, "timeout", timeoutInMillisecond};
        return getForEntity("tasks/poll/batch/{taskType}", params, taskList, taskType);
    }

    /**
     * @deprecated This API is deprecated and will be removed in the next version
     * use {@link #batchPollTasksInDomain(String, String, String, int, int)} instead
     */
    @Deprecated
    public List<Task> poll(String taskType, String domain, String workerId, int count, int timeoutInMillisecond) {
        return batchPollTasksInDomain(taskType, domain, workerId, count, timeoutInMillisecond);
    }

    /**
     * Batch poll for tasks in a domain. Batch size is configurable by count.
     *
     * @param taskType             Type of task to poll for
     * @param domain               The domain of the task type
     * @param workerId             Name of the client worker. Used for logging.
     * @param count                Maximum number of tasks to be returned. Actual number of tasks returned can be less than this number.
     * @param timeoutInMillisecond Long poll wait timeout.
     * @return List of tasks awaiting to be executed.
     */
    public List<Task> batchPollTasksInDomain(String taskType, String domain, String workerId, int count, int timeoutInMillisecond) {
        Preconditions.checkArgument(StringUtils.isNotBlank(taskType), "Task type cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(workerId), "Worker id cannot be blank");
        Preconditions.checkArgument(count > 0, "Count must be greater than 0");

        Object[] params = new Object[]{"workerid", workerId, "count", count, "timeout", timeoutInMillisecond, "domain", domain};
        return getForEntity("tasks/poll/batch/{taskType}", params, taskList, taskType);
    }

    /**
     * @deprecated This API is deprecated and will be removed in the next version
     * use {@link #getTaskDetails(String)} instead
     */
    @Deprecated
    public Task get(String taskId) {
        return getTaskDetails(taskId);
    }

    /**
     * Retrieve information about the task
     *
     * @param taskId ID of the task
     * @return Task details
     */
    public Task getTaskDetails(String taskId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(taskId), "Task id cannot be blank");
        return getForEntity("tasks/{taskId}", null, Task.class, taskId);
    }

    /**
     * @deprecated This API is deprecated and will be removed in the next version
     * use {@link #getPendingTasksByType(String, String, Integer)} instead
     */
    @Deprecated
    public List<Task> getTasks(String taskType, String startKey, Integer count) {
        return getPendingTasksByType(taskType, startKey, count);
    }

    /**
     * Retrieve pending tasks by type
     *
     * @param taskType Type of task
     * @param startKey id of the task from where to return the results. NULL to start from the beginning.
     * @param count    number of tasks to retrieve
     * @return Returns the list of PENDING tasks by type, starting with a given task Id.
     */
    public List<Task> getPendingTasksByType(String taskType, String startKey, Integer count) {
        Preconditions.checkArgument(StringUtils.isNotBlank(taskType), "Task type cannot be blank");

        Object[] params = new Object[]{"startKey", startKey, "count", count};
        return getForEntity("tasks/in_progress/{taskType}", params, taskList, taskType);
    }

    /**
     * Retrieve pending task identified by reference name for a workflow
     *
     * @param workflowId        Workflow instance id
     * @param taskReferenceName reference name of the task
     * @return Returns the pending workflow task identified by the reference name
     */
    public Task getPendingTaskForWorkflow(String workflowId, String taskReferenceName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(workflowId), "Workflow id cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(taskReferenceName), "Task reference name cannot be blank");

        return getForEntity("tasks/in_progress/{workflowId}/{taskRefName}", null, Task.class, workflowId, taskReferenceName);
    }


    /**
     * Updates the result of a task execution.
     *
     * @param taskResult TaskResults to be updated.
     */
    public void updateTask(TaskResult taskResult) {
        Preconditions.checkNotNull(taskResult, "Task result cannot be null");
        postForEntity("tasks", taskResult);
    }

    /**
     * @deprecated This API is deprecated and will be removed in the next version
     * use {@link #logMessageForTask(String, String)} instead
     */
    @Deprecated
    public void log(String taskId, String logMessage) {
        logMessageForTask(taskId, logMessage);
    }

    /**
     * Log messages for a task.
     *
     * @param taskId     id of the task
     * @param logMessage the message to be logged
     */
    public void logMessageForTask(String taskId, String logMessage) {
        Preconditions.checkArgument(StringUtils.isNotBlank(taskId), "Task id cannot be blank");
        postForEntity("tasks/" + taskId + "/log", logMessage);
    }

    /**
     * Ack for the task poll.
     *
     * @param taskId   Id of the task to be polled
     * @param workerId user identified worker.
     * @return true if the task was found with the given ID and acknowledged. False otherwise. If the server returns false, the client should NOT attempt to ack again.
     */
    public Boolean ack(String taskId, String workerId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(taskId), "Task id cannot be blank");

        String response = postForEntity("tasks/{taskId}/ack", null, new Object[]{"workerid", workerId}, String.class, taskId);
        return Boolean.valueOf(response);
    }

    /**
     * Retrieve all task definitions
     *
     * @return List of all the task definitions registered with the server
     */
    public List<TaskDef> getTaskDef() {
        return getForEntity("metadata/taskdefs", null, taskDefList);
    }

    /**
     * Retrieve the task definition of a given task type
     *
     * @param taskType type of task for which to retrieve the definition
     * @return Task Definition for the given task type
     */
    public TaskDef getTaskDef(String taskType) {
        Preconditions.checkArgument(StringUtils.isNotBlank(taskType), "Task type cannot be blank");
        return getForEntity("metadata/taskdefs/{tasktype}", null, TaskDef.class, taskType);
    }

    /**
     * Deletes a task type from the conductor server. Use with caution.
     *
     * @param taskType Task type to be unregistered.
     */
    public void unregisterTaskDef(String taskType) {
        Preconditions.checkArgument(StringUtils.isNotBlank(taskType), "Task type cannot be blank");
        delete("metadata/taskdefs/{tasktype}", taskType);
    }

    /**
     * Registers a set of task types with the conductor server
     *
     * @param taskDefs List of task types to be registered.
     */
    public void registerTaskDefs(List<TaskDef> taskDefs) {
        Preconditions.checkNotNull(taskDefs, "Task defs cannot be null");
        postForEntity("metadata/taskdefs", taskDefs);
    }

}
