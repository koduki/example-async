/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.example.async;

import cn.orz.pascal.example.async.utils.Task;
import cn.orz.pascal.example.async.utils.AsyncQueue;
import static cn.orz.pascal.example.async.utils.CommonUtils.*;
import cn.orz.pascal.example.async.utils.SimpleBench;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

/**
 *
 * @author koduki
 */
public class Test2 {

    static int dataSize = 5_000_000;

    public static void main(String[] args) throws Exception {
        System.out.println("Parallelism:" + ForkJoinPool.commonPool().getParallelism() + ", core:" + +ForkJoinPool.commonPool().getParallelism());
//        sequential();
//        async(1);
//        async(2);
        async(8);
//        async(16);
//        async(24);
//        async(48);
//        async(128);
    }

    public static String read(int index) {
        SimpleBench.begin("read");
        nanoSleep(100);
        SimpleBench.end("read");

        return index + ":success";
    }

    public static String parse(String msg) {
        SimpleBench.begin("parse");
        nanoSleep(150_000);
        nanoSleep(140_000);
        SimpleBench.end("parse");

        return msg + ":parsed";
    }

    public static void write(String msg) {
        SimpleBench.begin("write");
        SimpleBench.end("write");

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

    public static void async(int parallel) throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newFixedThreadPool(parallel);
        long s = System.nanoTime();
        AsyncQueue<String> asyncQueue = new AsyncQueue<>();

        List<CompletableFuture> futures = new ArrayList<>();
        for (int i = 0; i < dataSize; i++) {
            int index = i;
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                return read(index);
            }, es).thenAcceptAsync((String x) -> {
                asyncQueue.push(new Task(index, parse(x)));
            }, es);
            futures.add(future);
        }

        asyncQueue.startDequeue((x) -> write(x));
        asyncQueue.complete(() -> {
            long e = System.nanoTime();
            System.out.println("finish async(" + parallel + "):\t" + (e - s) / 1000 / 1000 + "\tms");
        });

        // call all futures
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        asyncQueue.pushEnd();

        es.shutdown();
    }

}
