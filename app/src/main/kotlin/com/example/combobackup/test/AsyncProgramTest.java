package com.example.combobackup.test;

import android.util.Log;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncProgramTest {

    private static ExecutorService executorService;

    // Task 1의 완료에 따른 콜백 -> 성공시 Task 2를 수행하게끔 하는 내용
    private static final CompletionHandler<String, Void> completionHandler = new CompletionHandler<String, Void>() {
        @Override
        public void completed(String result, Void attachment) {
            Log.d("Task 2 Start", "Task 1 Result = " + result);
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("Task 2 Stop", "");
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            Log.d("Task 1 Failed", exc.toString());
        }
    };

    public void doExecutorService()
    {
        executorService = Executors.newCachedThreadPool();
    }
}
