package com.dg;

import com.dg.api.ITaskProcessor;
import com.dg.result.TaskResult;

import java.util.Random;

public class MyTask implements ITaskProcessor<Integer,Integer> {
    @Override
    public TaskResult<Integer> taskExecute(Integer param) {
        Random r = new Random();
        int flag = r.nextInt(500);
        SleepTools.sleepMs(flag);
        if(flag<=300){
            Integer returnValue = param.intValue() + flag;
            return TaskResult.<Integer>success(returnValue);
        }else if(flag<=400){
            return TaskResult.<Integer>failure(-1, "Failure");
        }else{
            try {
                throw  new RuntimeException("异常发生了");
            } catch (RuntimeException e) {
                return TaskResult.<Integer>exception(-1, e);
            }
        }
    }
}
