/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.example.async.utils;

import cn.orz.pascal.example.async.Test1;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author koduki
 */
public class CommonUtils {

    public static void sleep(final long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException ex) {
            Logger.getLogger(Test1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void nanoSleep(final long interval) {
        long start = System.nanoTime();
        long end = 0;
        do {
            end = System.nanoTime();
        } while (start + interval >= end);
    }
}
