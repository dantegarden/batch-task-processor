package com.dg.framework;

import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
/**放到DelayQueue的元素**/
@Data
public class ItemVO<T> implements Delayed {
    private long activeTime; //到期时刻，毫秒
    private T data;

    //传入duration 过期时长
    public ItemVO(long duration, T data) {
        this.activeTime = TimeUnit.NANOSECONDS.convert(duration, TimeUnit.MILLISECONDS)
                + System.nanoTime(); //到期时长+当前时间=到期时刻
        this.data = data;
    }

    //求元素的剩余时间
    @Override
    public long getDelay(TimeUnit unit) {
        //剩余时间=到期时刻-当前时间
        long remain = unit.convert(activeTime-System.nanoTime(), TimeUnit.NANOSECONDS);
        return remain;
    }

    //按照剩余时间排序
    @Override
    public int compareTo(Delayed o) {
        long d = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        return d==0 ? 0 : (d>0?1:-1);
    }
}
