/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.example.async.utils;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 *
 * @author koduki
 */
public class AsyncQueue<T> {

    private Queue<Task<T>> pq = new PriorityQueue<>();
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Runnable completeCallback;

    synchronized public void push(Task task) {
        pq.offer(task);
    }

    synchronized public Task pop() {
        return pq.poll();
    }

    synchronized public Task peek() {
        return pq.peek();
    }

    synchronized public boolean isEmpty() {
        return pq.isEmpty();
    }

    synchronized public boolean isNotEnd() {
        if (pq.isEmpty()) {
            return true;
        } else {
            return pq.peek().getData() != null;
        }
    }

    @Override
    synchronized public String toString() {
        return pq.toString();
    }

    public void startDequeue(Consumer<T> consumer) {
        new Thread(() -> {
            int prevId = -1;
            boolean fastable = true;
            logger.fine("dequeue thread:start");
            while (this.isNotEnd()) {
                if (!pq.isEmpty() && (fastable | this.peek().getSequentialNumber() == (prevId + 1))) {
                    Task<T> task = this.pop();
                    consumer.accept(task.getData());
                    if (fastable) {
                        fastable = false;
                        prevId = task.getSequentialNumber();
                    } else {
                        prevId++;
                    }
                }
            }
            this.completeCallback.run();
            logger.fine("dequeue thread:close");
        }).start();
    }

    public void complete(Runnable callback) {
        this.completeCallback = callback;
    }

    public void pushEnd() {
        this.push(new Task(Integer.MAX_VALUE, null));
    }

}
