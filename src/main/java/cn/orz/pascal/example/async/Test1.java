/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.example.async;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import cn.orz.pascal.example.async.utils.AsyncQueue;
import cn.orz.pascal.example.async.utils.Task;
import java.util.concurrent.CompletableFuture;
import static cn.orz.pascal.example.async.utils.CommonUtils.*;

/**
 *
 * @author koduki
 */
public class Test1 {

    public static void main(String[] args) throws Exception {
        AsyncQueue<String> asyncQueue = new AsyncQueue<>();

        asyncQueue.startDequeue((x) -> System.out.println("step3(orderd):" + x));

        CompletableFuture<Void> future1 = CompletableFuture.supplyAsync(() -> {
            String msg = "future1";
            sleep(1000);
            System.out.println("step1(async):" + msg);
            return new Task(1, msg);
        }).thenAcceptAsync((Task task) -> {
            sleep(1);
            System.out.println("step2(async):" + task.getData());
            asyncQueue.push(task);
        });

        CompletableFuture<Void> future2 = CompletableFuture.supplyAsync(() -> {
            String msg = "future2";
            sleep(500);
            System.out.println("step1(async):" + msg);
            return new Task(2, msg);
        }).thenAcceptAsync((Task task) -> {
            sleep(2000);
            System.out.println("step2(async):" + task.getData());
            asyncQueue.push(task);
        });
        CompletableFuture<Void> future3 = CompletableFuture.supplyAsync(() -> {
            String msg = "future3";
            sleep(1200);
            System.out.println("step1(async):" + msg);
            return new Task(3, msg);
        }).thenAcceptAsync((Task item) -> {
            sleep(100);
            System.out.println("step2(async):" + item.getData());
            asyncQueue.push(item);
        });
        CompletableFuture.allOf(future1, future2, future3).get();
        System.out.println("all future done.");

        asyncQueue.pushEnd();
    }

}
