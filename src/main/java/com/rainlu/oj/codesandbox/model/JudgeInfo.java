package com.rainlu.oj.codesandbox.model;

import lombok.Data;

/**
 * @description 判题过程中得到的一些信息，比如：程序的失败原因？程序执行所消耗的内存、空间？
 * @author Jun Lu
 * @date 2023-09-03 18:37:04
 */
@Data
public class JudgeInfo {

    /**
     * 程序的执行信息：com.rainlu.model.enums.JudgeInfoMessageEnum
     */
    private String message;

    /**
     * 消耗内存
     */
    private Long memory;

    /**
     * 消耗时间（KB）
     */
    private Long time;
}
