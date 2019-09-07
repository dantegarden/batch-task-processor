package com.dg.api;

import com.dg.result.TaskResult;

/**
 * 要求框架使用者实现的任务接口，因为任务的性质在调用时才知道
 * 所以传入的参数和返回值都使用泛型
 * */
public interface ITaskProcessor<T, R> {
    TaskResult<R> taskExecute(T param);
}
