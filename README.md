# batch-task-processor
batch-task-processor是一个用于处理批量任务的多线程框架。

## 使用方法

clone仓库源码，打包并将依赖安装到本地maven仓库。

```mvn clean install -Dmaven.test.skip ```

在需要使用此框架的项目中引入依赖
```
<dependency>
    <groupId>com.dg</groupId>
    <artifactId>batch-task-processor</artifactId>
    <version>1.0</version>
</dependency>
```
注入框架核心组件
```
@Autowired
private PendingJobPool pool;
```

写一个自定义的任务处理类，实现```ITaskProcessor```接口，并覆盖```taskExecute```方法。
其中```ITaskProcessor```规定了两个泛型，入参类型和```taskExecute```方法的返回值类型。
例如：
```
public class MyTask implements ITaskProcessor<Integer,Integer> {
    @Override
    public TaskResult<Integer> taskExecute(Integer param) {
        Random r = new Random();
        int flag = r.nextInt(15);
        if(flag<=5){
            Integer returnValue = param.intValue() + flag;
            return TaskResult.<Integer>success(returnValue);
        }else if(flag<=10){
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
```

在主线程中，注册工作，并提交任务到框架。由框架来调度执行。
```
MyTask myTask = new MyTask();
pool.registerJob(JOB_NAME, TASK_LENGTH, myTask, 1000*5);
for (int i = 0; i < TASK_LENGTH; i++) {
    pool.putTask(JOB_NAME, new Random().nextInt());
}
```

轮询状态，直到任务完成。取得处理结果。
```
while(true){
    if(pool.isJobFinished(JOB_NAME))break;
}
System.out.println(pool.getJobProgress(JOB_NAME).toString());
return pool.getJobResult(JOB_NAME);
```

## 注意事项

1. 框架使用了springboot2.1.4为基础依赖，但本身没有与springboot的版本绑死。在发生冲突时，可以先修改springboot版本再安装。
对于使用spring的项目，也可以剔除springboot依赖。
2. 当使用者注册的工作完成时，为避免撑爆内存，框架只会将结果保存一定时间，过期会自动清除。保存时间在注册任务时传入。
3. ```pool.getJobProgress(String jobName)```用于获取工作进度，将会返回成功数、失败数、完成数、总数四项信息，可配合前端组件实现进度条之类的功能。
4. 框架默认使用spring容器，对使用spring的项目来说，需要扫描包下的bean。
```
@Configuration
@ComponentScan(value = {"com.dg"})
public class DgConfig {
}
```
而对于不使用spring的项目，只需要将PendingJobPool中有关spring的注解去除，将单例模式的注释段打开，把获取实例的地方改成从单例方法获取即可。