package com.dg.framework;

import com.dg.api.ITaskProcessor;
import com.dg.job.JobInfo;
import com.dg.job.JobProgress;
import com.dg.result.TaskResult;
import com.dg.result.TaskResultType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/***
 * 框架主体
 * */
@Component
@Slf4j
public class PendingJobPool {

    private static final int DEFAULT_SIZE = 5000;
    private static final int THREAD_SIZE = Runtime.getRuntime().availableProcessors() + 1;
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>(DEFAULT_SIZE);

    /**自定义线程池
     * 固定大小，有界队列
     * **/
    private ExecutorService pool =
            new ThreadPoolExecutor(THREAD_SIZE, THREAD_SIZE, 60, TimeUnit.SECONDS, taskQueue);

    //job的存放容器
    private static ConcurrentHashMap<String, JobInfo<?>> jobInfoMap = new ConcurrentHashMap<>();

    //单例模式
//    private PendingJobPool() {
//    }
//    private static class PendingJobPoolHolder{
//        private static PendingJobPool instance = new PendingJobPool();
//    }
//    public static PendingJobPool getInstance(){
//        return PendingJobPoolHolder.instance;
//    }

    //检查过期工作
    @Autowired
    private CheckJobProcesser checkJobProcesser; // CheckJobProcesser.getInstance()
//    static {
//        checkJobProcesser.start();
//    }

    /**使用者注册工作*/
    public <R> void registerJob(String jobName, int taskLength, ITaskProcessor<?,?> taskProcesser, long expireTime){
        JobInfo<R> jobInfo = new JobInfo<>(jobName, taskLength, taskProcesser, expireTime);
        JobInfo<?> oldJobInfo = jobInfoMap.putIfAbsent(jobName, jobInfo);
        if (oldJobInfo != null) {
            throw new RuntimeException(jobName + "已经注册过了！");
        }
    }

    /**获取注册的job**/
    private <R> JobInfo<R> getJob(String jobName){
        JobInfo<R> jobInfo = (JobInfo<R>) jobInfoMap.get(jobName);
        if(jobInfo == null){
            throw new RuntimeException(jobInfo +"任务未注册！");
        }
        return jobInfo;
    }

    /**使用者提交工作中的任务*/
    public <T, R> void putTask(String jobName, T taskParam){
        JobInfo<R> jobInfo = getJob(jobName);
        PendingTask<T, R> pendingTask = new PendingTask<>(jobInfo, taskParam); //包装成Runnable
        pool.execute(pendingTask); //交给线程池执行
    }

    /**获得任务处理结果*/
    public <R> List<TaskResult<R>> getJobResult(String jobName){
        return getJob(jobName).getTaskResults();
    }

    /**获取任务进度情况*/
    public JobProgress getJobProgress(String jobName){
        return getJob(jobName).jobProgress();
    }

    /**获取任务完成状态*/
    public boolean isJobFinished(String jobName){
        return jobInfoMap.get(jobName).isFinished();
    }

    /**获取工作注册容器**/
    public static Map<String,JobInfo<?>> getJobMap(){
        return jobInfoMap;
    }

    //对job中的任务进行包装，提交给线程池使用，并处理任务的结果，写入缓存以供查询
    private class PendingTask<T, R> implements Runnable{

        private JobInfo<R> jobInfo;
        private T taskParam;

        public PendingTask(JobInfo<R> jobInfo, T taskParam) {
            this.jobInfo = jobInfo;
            this.taskParam = taskParam;
        }

        @Override
        public void run() {

            ITaskProcessor<T, R> taskProcessor = (ITaskProcessor<T, R>) jobInfo.getTaskProcessor();
            TaskResult<R> taskResult = null;
            try {
                //调用使用者自己实现的业务方法
                taskResult = taskProcessor.taskExecute(taskParam);
                //要做检查，防止使用者处理不当
                if (taskResult == null) {
                    taskResult = TaskResult.<R>exception(null, "The task result is null");
                }else if(taskResult.getResultType() == null){
                    if(StringUtils.isEmpty(taskResult.getReason())){
                        taskResult = TaskResult.<R>exception(null, "Thought task result isn't null, but reason is null");
                    }else{
                        taskResult = TaskResult.<R>exception(null, taskResult.getReason());
                    }
                }
            } catch (Exception e) {
                //异常处理
                e.printStackTrace();
                taskResult = TaskResult.<R>exception(null, e);
            }finally {
                //放入结果队列
                jobInfo.addTaskResult(taskResult);
                if(jobInfo.isFinished()){
                    checkJobProcesser.putJob(jobInfo);
                }
            }
        }
    }

}
