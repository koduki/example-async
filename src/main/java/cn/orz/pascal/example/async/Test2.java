/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.example.async;

import cn.orz.pascal.example.async.utils.Task;
import cn.orz.pascal.example.async.utils.AsyncQueue;
import static cn.orz.pascal.example.async.utils.CommonUtils.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author koduki
 */
public class Test2 {

    static int dataSize = 1_000_000;

    public static void main(String[] args) throws Exception {
        sequential();
        async();
    }

    public static String read(int index) {
        nanoSleep(100);
        return index + ":success";
    }

    public static String parse(String msg) {
        nanoSleep(150_000);
        nanoSleep(140_000);
        return msg + ":parsed";
    }

    public static void write(String msg) {
//        System.out.println(msg);
    }

    public static void sequential() {
        long s = System.nanoTime();
        for (int i = 0; i < dataSize; i++) {
            String msg = read(i);
            write(parse(msg));
        }
        long e = System.nanoTime();

        System.out.println("finish sequential:\t" + (e - s) / 1000 / 1000 + "\tms");
    }

    public static void async() throws InterruptedException, ExecutionException {
        long s = System.nanoTime();
        AsyncQueue<String> asyncQueue = new AsyncQueue<>();

        List<CompletableFuture> futures = new ArrayList<>();
        for (int i = 0; i < dataSize; i++) {
            int index = i;
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                return read(index);
            }).thenAcceptAsync((String x) -> {
                asyncQueue.push(new Task(index, parse(x)));
            });
            futures.add(future);
        }

        asyncQueue.startDequeue((x) -> write(x));
        asyncQueue.complete(() -> {
            long e = System.nanoTime();
            System.out.println("finish sequential:\t" + (e - s) / 1000 / 1000 + "\tms");
        });

        // call all futures
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        asyncQueue.pushEnd();

    }

}
