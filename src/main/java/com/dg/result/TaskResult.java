package com.dg.result;

import lombok.Data;

/**任务处理返回结果的实体类**/
@Data
public class TaskResult <R> {
    //方法执行结果类型
    private final TaskResultType resultType;
    //方法返回的业务结果
    private final R returnValue;
    //方法失败的原因
    private final String reason;

    public TaskResult(TaskResultType resultType, R returnValue, String reason) {
        this.resultType = resultType;
        this.returnValue = returnValue;
        this.reason = reason;
    }
    public static <R> TaskResult<R> success(R returnValue) {
        return new TaskResult<R>(TaskResultType.SUCCESS, returnValue, "Success");
    }

    public static <R> TaskResult<R> failure(R returnValue) {
        return new TaskResult<R>(TaskResultType.FAILURE, returnValue, "Failure");
    }

    public static <R> TaskResult<R> failure(R returnValue, String reason) {
        return new TaskResult<R>(TaskResultType.FAILURE, returnValue, reason);
    }

    public static <R> TaskResult<R> exception(R returnValue, String reason) {
        return new TaskResult<R>(TaskResultType.EXCEPTION, returnValue, reason);
    }

    public static <R> TaskResult<R> exception(R returnValue, Exception e) {
        return new TaskResult<R>(TaskResultType.EXCEPTION, returnValue, e.getMessage());
    }

    @Override
    public String toString() {
        return "TaskResult{" +
                "resultType=" + resultType +
                ", returnValue=" + returnValue +
                ", reason='" + reason + '\'' +
                '}';
    }
}
