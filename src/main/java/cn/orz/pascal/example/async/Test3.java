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

/**
 *
 * @author koduki
 */
public class Test3 {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("system");
      
        ActorRef actor = system.actorOf(Props.create(SimpleActor.class), "simpleActor");

        String message = "hello.";
        actor.tell(message, null);

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        system.shutdown();

        System.out.println("end.");
    }

    public static class SimpleActor extends UntypedActor {

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof String) {
                System.out.println("message:" + message);
            } else {
                System.out.println("unhandled message.");

                // 想定しない型のメッセージはスルーする
                unhandled(message);
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
