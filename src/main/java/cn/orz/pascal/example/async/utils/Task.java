/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.example.async.utils;

/**
 *
 * @author koduki
 */
public class Task<T> implements Comparable<Task<T>> {

    private int sequentialNumber;
    private T data;

    public Task(int sequentialNumber, T data) {
        this.sequentialNumber = sequentialNumber;
        this.data = data;
    }

    public int getSequentialNumber() {
        return sequentialNumber;
    }

    public T getData() {
        return data;
    }

    @Override
    public int compareTo(Task o) {
        return this.sequentialNumber - o.sequentialNumber;
    }

    @Override
    public String toString() {
        return "[" + this.sequentialNumber + ", " + data + "]";
    }

}
