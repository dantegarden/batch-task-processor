package com.dg;

import com.dg.framework.PendingJobPool;
import com.dg.result.TaskResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Random;

//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class AppTest {
//
//    private static final String JOB_NAME = "MY_NAME";
//    private static final int TASK_LENGTH = 100;
//
//    //查进度的线程
//    static class QueryResult implements Runnable{
//        private PendingJobPool pool;
//
//        public QueryResult(PendingJobPool pool) {
//            this.pool = pool;
//        }
//
//        @Override
//        public void run() {
//            int count = 0; //查询次数
//            while (count<350){
//                List<TaskResult<String>> jobResult = pool.getJobResult(JOB_NAME);
//                if(!jobResult.isEmpty()){
//                    System.out.println(pool.getJobProgress(JOB_NAME));
//                    System.out.println(jobResult);
//                }
//                SleepTools.sleepMs(100);
//                count++;
//            }
//        }
//    }
//
//    @Autowired
//    private PendingJobPool pool;
//
//    @Test
//    public void test(){
//        MyTask myTask = new MyTask();
//        //拿到框架实例
//        //PendingJobPool pool = PendingJobPool.getInstance();
//        //注册job
//        pool.registerJob(JOB_NAME, TASK_LENGTH, myTask, 1000*5);
//        Random r = new Random();
//        for (int i = 0; i < TASK_LENGTH; i++) { //依次推入Task
//            pool.putTask(JOB_NAME, r.nextInt());
//        }
//        //负责查询进度
//        Thread t = new Thread(new QueryResult(pool));
//        t.start();
//        //轮询直到任务完成
//        while(true){
//            if(pool.isJobFinished(JOB_NAME))break;
//            SleepTools.sleepMs(100);
//        }
//        System.out.println(pool.getJobProgress(JOB_NAME));
//        pool.getJobResult(JOB_NAME).stream().forEach(System.out::println);
//    }
//}
