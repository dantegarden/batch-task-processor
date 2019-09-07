package com.dg.job;

import com.dg.api.ITaskProcessor;
import com.dg.framework.CheckJobProcesser;
import com.dg.result.TaskResult;
import com.dg.result.TaskResultType;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class JobInfo<R> {
    //工作的唯一标识
    private final String jobName;
    //工作的任务个数
    private final int taskLength;
    //工作的实际内容，要求使用者实现
    private final ITaskProcessor<?,?> taskProcessor;
    //处理成功的总数
    private AtomicInteger successCount;
    //已处理的总数
    private AtomicInteger processedCount;
    //保存工作中每个任务的结果
    private LinkedBlockingDeque<TaskResult<R>> taskResultQueue;
    //过期时间 毫秒，要求使用者传入
    private final long expireTime;

    public JobInfo(String jobName, int taskLength,
                   ITaskProcessor<?, ?> taskProcessor, long expireTime) {
        this.jobName = jobName;
        this.taskLength = taskLength;
        this.taskProcessor = taskProcessor;
        this.expireTime = expireTime;
        this.taskResultQueue =  new LinkedBlockingDeque<TaskResult<R>>(taskLength);
        this.successCount = new AtomicInteger(0);
        this.processedCount = new AtomicInteger(0);
    }

    public ITaskProcessor<?, ?> getTaskProcessor() {
        return taskProcessor;
    }

    /**任务执行成功的个数**/
    public int getSuccessCount() {
        return successCount.get();
    }

    /**任务已执行的个数**/
    public int getProcessedCount() {
        return processedCount.get();
    }

    /**任务执行失败的个数**/
    public int getFailureCount(){
        return processedCount.get() - successCount.get();
    }

    /**工作是否完成*/
    public boolean isFinished(){
        return processedCount.get() == taskLength;
    }

    /**拿结果**/
    public <R> List<TaskResult<R>> getTaskResults(){
        List<TaskResult<R>> taskList = Lists.newLinkedList();
        TaskResult taskResult;
        //拿结果从头拿，放结果从尾放
        while((taskResult=taskResultQueue.pollFirst())!=null){
            taskList.add(taskResult);
        }
        return taskList;
    }

    /**放结果
     * 无锁，只能保证最终一致性*/
    public void addTaskResult(TaskResult<R> result){
        if(TaskResultType.SUCCESS.equals(result.getResultType())){
            successCount.incrementAndGet();
        }
        taskResultQueue.addLast(result);
        processedCount.incrementAndGet();
    }

    /**获得进度信息*/
    public JobProgress jobProgress() {
        return JobProgress.newInstance(successCount, processedCount, taskLength);
    }

    public String getJobName() {
        return jobName;
    }

    public long getExpireTime() {
        return expireTime;
    }

}
