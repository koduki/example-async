/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.example.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author koduki
 */
public class AsyncTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newCachedThreadPool();
        String msg = "hello";
        Future<String> f = es.submit(() -> {
            System.out.println("execute call");
            return msg + "world";
        });
        Thread.sleep(3000);
        System.out.println("get:"+f.get() + "2");
        es.shutdown();
    }
}
