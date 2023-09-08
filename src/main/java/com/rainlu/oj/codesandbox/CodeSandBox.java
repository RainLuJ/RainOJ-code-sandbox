package com.rainlu.oj.codesandbox;

import com.rainlu.oj.codesandbox.model.ExecuteCodeRequest;
import com.rainlu.oj.codesandbox.model.ExecuteCodeResponse;

/**
 * @description 定义代码沙箱应该遵循的设计规范
 * @author Jun Lu
 * @date 2023-09-03 18:28:47
 */
public interface CodeSandBox {

    /**
     * @description 代码沙箱执行代码的逻辑
     * @author Jun Lu
     * @date 2023-09-03 18:40:06
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}
