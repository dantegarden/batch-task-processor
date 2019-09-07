package com.dg.job;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class JobProgress {
    private int success;
    private int failure;
    private int process;
    private int total;

    private JobProgress() {
    }

    private JobProgress(int success, int process, int failure,  int total) {
        this.success = success;
        this.failure = failure;
        this.process = process;
        this.total = total;
    }

    public static JobProgress newInstance(AtomicInteger successCount, AtomicInteger processCount, int total) {
        int success = successCount.get();
        int process = processCount.get();
        return new JobProgress(success, process, process - success, total);
    }

    @Override
    public String toString() {
        return String.format("success=%d, failure=%d, process=%d, total=%d", success,failure,process,total);
    }
}
