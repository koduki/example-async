/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.example.async;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import static cn.orz.pascal.example.async.Test2.dataSize;
import cn.orz.pascal.example.async.utils.Task;
import cn.orz.pascal.example.async.utils.AsyncQueue;
import static cn.orz.pascal.example.async.utils.CommonUtils.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.runtime.AbstractFunction1;

/**
 *
 * @author koduki
 */
public class Test3 {

    static int dataSize = 10_000;

    public static void main(String[] args) throws Exception {
        System.out.println("Parallelism:" + ForkJoinPool.commonPool().getParallelism() + ", core:" + +ForkJoinPool.commonPool().getParallelism());
//        sequential();
        for (int i = 0; i < 24; i++) {
            async(i + 1);
        }

//        async(48);
//        async(128);
        asyncAkka();
    }

    public static String read(int index) {
        nanoSleep(150_000);
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

    public static void parallel() {
        int parallel = 3;
        long s = System.nanoTime();
        ExecutorService es = Executors.newFixedThreadPool(parallel);

        List<java.util.concurrent.Future> futures = new ArrayList<>();
        for (int n = 0; n < parallel; n++) {
            java.util.concurrent.Future f = es.submit(() -> {
                for (int i = 0; i < dataSize / parallel; i++) {
                    String msg = read(i);
                    write(parse(msg));
                }
            });
            futures.add(f);
        }
//        CompletableFuture.runAsync(null)
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

    public static void asyncAkka() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutionContext ec = scala.concurrent.ExecutionContext.Implicits$.MODULE$.global();
        final int parallel = scala.concurrent.forkjoin.ForkJoinPool.commonPool().getParallelism();
        AsyncQueue<String> asyncQueue = new AsyncQueue<>();
        ActorSystem system = ActorSystem.create("system");
        ActorRef actor = system.actorOf(Props.create(SimpleActor.class), "simpleActor");
        long s = System.nanoTime();

        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < dataSize; i++) {
            Integer index = i;
            Future future = Patterns.ask(actor, index, 5000)
                    .map(new AbstractFunction1() {
                        @Override
                        public Object apply(Object x) {
                            asyncQueue.push(new Task(index, parse((String) x)));
                            return null;
                        }
                    }, ec);

            futures.add(future);
        }

        asyncQueue.startDequeue((x) -> write(x));
        asyncQueue.complete(() -> {
            long e = System.nanoTime();
            System.out.println("finish asyncAkka(" + parallel + "):\t" + (e - s) / 1000 / 1000 + "\tms");
        });

        // call all futures
        futures.stream().parallel().forEach((Future future) -> {
            try {
                Await.ready(future, Duration.create(5000, TimeUnit.MILLISECONDS));
            } catch (TimeoutException | InterruptedException ex) {
                Logger.getLogger(ExampleActor.class.getName()).log(Level.SEVERE, null, ex);
            }

        });
        asyncQueue.pushEnd();
        system.shutdown();

    }

    public static class SimpleActor extends UntypedActor {

        int state = 0;

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof Integer) {
                String result = read((Integer) message);
                sender().tell(result, self());
            } else {
                System.out.println("unhandled message1" + message.getClass().getSimpleName());
                //unhandled(message);
                sender().tell(++state, self());
            }
        }

        @Override
        public void preStart() {
//            System.out.println("preStart");
        }

        @Override
        public void postStop() {
//            System.out.println("postStop");
        }

    }

}
