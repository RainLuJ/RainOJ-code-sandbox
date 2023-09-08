package com.rainlu.oj.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description 代码沙箱执行代码时所需的参数的实体类（即：调用方传递的调用参数）
 * @author Jun Lu
 * @date 2023-09-03 18:30:43
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {

    /**
     * 允许接收一组输入用例
     */
    private List<String> inputList;

    /**
     * 用户提交的代码
     */
    private String code;

    /**
     * 用户选择的语言
     */
    private String language;
}
