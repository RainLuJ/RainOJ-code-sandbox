package com.rainlu.oj.codesandbox.unsafeCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @description 无限占用内存
 * @author Jun Lu
 */
public class MemoryError {

    public static void main(String[] args) throws InterruptedException {
        List<byte[]> bytes = new ArrayList<>();
        while (true) {
            bytes.add(new byte[10000]);
        }
    }

}
