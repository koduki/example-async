/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.orz.pascal.example.async.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author koduki
 */
public class SimpleBench {

    private static Map<String, Long> summary = Collections.synchronizedMap(new HashMap<>());

    public static void begin(String tag) {
        long s = System.nanoTime();
        if (!summary.containsKey(tag)) {
            summary.put(tag, 0L);
        }

        summary.put(tag, summary.get(tag) - s);
    }

    public static void end(String tag) {
        long e = System.nanoTime();

        summary.put(tag, summary.get(tag) + e);
    }
}
