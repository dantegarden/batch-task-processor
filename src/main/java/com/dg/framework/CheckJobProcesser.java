package com.dg.framework;

import com.dg.job.JobInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;

/**任务完成后，在一定的时间内提供查询，之后释放资源
 * 定期地处理过期任务
 * */
@Component
@Slf4j
public class CheckJobProcesser implements InitializingBean {
    //存放已完成的任务，在这里等待过期
    private DelayQueue<ItemVO<String>> queue = new DelayQueue<>();

    //单例模式
//    private CheckJobProcesser() {
//    }
//
//    private static class CheckJobProcesserHolder{
//        private static CheckJobProcesser instance = new CheckJobProcesser();
//    }
//
//    public static CheckJobProcesser getInstance(){
//        return CheckJobProcesserHolder.instance;
//    }
    
    public void putJob(JobInfo jobInfo){
        ItemVO<String> item = new ItemVO<>(jobInfo.getExpireTime(), jobInfo.getJobName());
        queue.offer(item);
        log.info("Job[{}] 已经加入过期检查队列, {}ms后过期",  jobInfo.getJobName(), jobInfo.getExpireTime());
    }

    /**处理到期任务的实现**/
    private class FetchJob implements Runnable{

        @Override
        public void run() {
            while(true){
                try{
                    ItemVO<String> item = queue.take();
                    String jobName = item.getData(); //拿到已过期的任务标识
                    PendingJobPool.getJobMap().remove(jobName);
                    log.info("Job[{}] 过期，已对其进行了清除", jobName);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**开启过期检查线程*/
//    public void start(){
//        Thread checkThread = new Thread(new FetchJob());
//        checkThread.setDaemon(true); //与主线程同生同死
//        log.info("开启守护线程，对过期任务做检查");
//    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread checkThread = new Thread(new FetchJob());
        checkThread.setDaemon(true); //与主线程同生同死
        checkThread.start();
        log.info("开启守护线程，对过期任务做检查");
    }
}
