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
import cn.orz.pascal.example.async.utils.CommonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import scala.Function1;
import scala.PartialFunction;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.impl.ExecutionContextImpl;
import scala.runtime.AbstractFunction1;

/**
 *
 * @author koduki
 */
public class ExampleActor {

    private static Integer result = 0;

    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("system");

        ActorRef actor = system.actorOf(Props.create(SimpleActor.class), "simpleActor");

        String message = "hello.";

        ExecutionContext ec = scala.concurrent.ExecutionContext.Implicits$.MODULE$.global();

        // メッセージを送信し、結果を受け取る
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int index = i;
            Future future = Patterns.ask(actor, message, 5000)
                    .map(new AbstractFunction1() {
                        @Override
                        public Object apply(Object t1) {
                            long sleep = Math.round(Math.random()%100_000_000);
                            CommonUtils.nanoSleep(sleep);
                            System.out.println("Hello Function" + index);
                            return null;
                        }
                    }, ec);
            futures.add(future);
        }
        futures.stream().parallel().forEach((Future future) -> {
            try {
                Await.ready(future, Duration.create(5000, TimeUnit.MILLISECONDS));
            } catch (TimeoutException ex) {
                Logger.getLogger(ExampleActor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ExampleActor.class.getName()).log(Level.SEVERE, null, ex);
            }

        });
        
      
//        System.out.println("result=" + result);

        // 想定しない型を送信する。Exceptionが発生する
        result = (Integer) Await.result(Patterns.ask(actor, 1, 5000), Duration.create(5000, TimeUnit.MILLISECONDS));
        System.out.println("result=" + result);

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        system.shutdown();

        System.out.println(
                "end.");
    }

    public static class SimpleActor extends UntypedActor {

        int state = 0;

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof String) {
                System.out.println("message:" + message);
                sender().tell(++state, self());
            } else {
                System.out.println("unhandled message.");
                unhandled(message);
                sender().tell(++state, self());
            }
        }

        @Override
        public void preStart() {
            System.out.println("preStart");
        }

        @Override
        public void postStop() {
            System.out.println("postStop");
        }

    }
}
