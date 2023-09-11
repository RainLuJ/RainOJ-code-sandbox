package com.rainlu.oj.codesandbox.unsafeCode;

/**
 * @description 无限睡眠（阻塞程序执行）
 * @author Jun Lu
 */
public class InfiniteSleepError {

    public static void main(String[] args) throws InterruptedException {
        long ONE_HOUR = 60 * 60 * 1000L;
        Thread.sleep(ONE_HOUR);
        System.out.println("起床！");
    }

}
