package com.rainlu.oj.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description 代码沙箱执行代码后的返回结果（代码沙箱只负责根据`程序 + 输入用例`产出执行结果）
 * @author Jun Lu
 * @date 2023-09-03 18:30:17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

    /**
     * 一组用例 + 程序 == 一组程序的执行结果
     */
    private List<String> outputList;

    /**
     * 接口信息
     *  - 程序的执行信息
     *  - 接口本身的状态信息
     */
    private String message;

    /**
     * 执行状态
     *  - 失败？
     *  - 成功？
     *  - ……
     */
    private Integer status;

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;
}
