package com.rainlu.oj.codesandbox.model;


import lombok.Data;

/**
 * 封装程序在执行时的相关信息，再根据这些信息来封装JudgeInfo
 */
@Data
public class ExecuteMessage {

    /**
     * 退出码
     */
    private Integer exitValue;

    /**
     * 打印到控制台上的正常输出消息(程序执行结果)
     */
    private String message;

    /**
     * 打印到控制台上的错误消息
     */
    private String errorMessage;

    /**
     * 程序执行时间
     */
    private Long time;

    /**
     * 程序执行所消耗的内存
     */
    private Long memory;
}
