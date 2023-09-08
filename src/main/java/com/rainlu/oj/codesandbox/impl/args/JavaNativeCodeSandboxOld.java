package com.rainlu.oj.codesandbox.impl.args;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.rainlu.oj.codesandbox.CodeSandBox;
import com.rainlu.oj.codesandbox.model.ExecuteCodeRequest;
import com.rainlu.oj.codesandbox.model.ExecuteCodeResponse;
import com.rainlu.oj.codesandbox.model.ExecuteMessage;
import com.rainlu.oj.codesandbox.model.JudgeInfo;
import com.rainlu.oj.codesandbox.utils.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JavaNativeCodeSandboxOld implements CodeSandBox {

    // 暂时用来存放用户提交的代码的目录
    private static final String GLOBAL_CODE_DIR_PATH = "tempCode";

    private static final String GLOBAL_JAVA_FILE_NAME = "Main.java";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        /* 1. 创建项目目录 */
        // 获取当前项目的绝对路径
        String curProjectDir = System.getProperty("user.dir");
        // File.separator：获取不同系统中的文件分隔符
        String globalCodePath = curProjectDir + File.separator + GLOBAL_CODE_DIR_PATH;
        // 判断目录是否存在？不存在则新建
        if (!FileUtil.exist(globalCodePath)) {
            FileUtil.mkdir(globalCodePath);
        }

        /* 2. 指定用户提交的文件路径 */
        // 由于每个用户提交的文件都是"Main.java"，所以需要隔离存放文件的目录
        String userCodeParentPath = globalCodePath + File.separator + UUID.randomUUID();
        // 拼接存放用户提交的Main.java文件所要存放在本地的路径
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_FILE_NAME;
        // 将用户提交的代码写入本地"Main.java"文件中
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        /* 3. 编译"Main.java"文件 */
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);

            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);

        } catch (Exception e) {
            return getErrorResponse(e);
        }


        /* 4. 运行"Main.java"文件 */
        // 封装每组用例的执行信息
        List<ExecuteMessage> executeMessageList = new LinkedList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Dfile.encoding=utf-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                executeMessageList.add(executeMessage);
                System.out.println(executeMessage);
            } catch (Exception e) {
                return getErrorResponse(e);
            }
        }

        /* 5. 根据每组用例的执行消息来收集整理程序执行的输出结果 */
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> ouputList = new LinkedList<>();
        long maxExecuteTime = Long.MIN_VALUE;
        for (ExecuteMessage executeMessage : executeMessageList) {
            // 程序执行，控制台正常输出的消息就是程序的执行结果
            String message = executeMessage.getMessage();
            String errorMessage = executeMessage.getErrorMessage();
            Long time = executeMessage.getTime();

            // 如果执行消息中有错误消息，则封装错误消息，并break，跳出循环
            if (StrUtil.isNotBlank(errorMessage)) {
                // 用户提交的代码在【运行】中存在错误
                executeCodeResponse.setStatus(3);
                executeCodeResponse.setMessage(errorMessage);
                break;
            }

            maxExecuteTime = Math.max(maxExecuteTime, time);
            ouputList.add(message);
        }

        // 如果每组用例执行后都没有产生“错误消息”，ouputList会与executeMessageList的size相等（因为没有提前break）
        if (ouputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(1);
        }

        // 不管所有的用例都有没有执行成功，这里都封装能正确执行的用例输出
        executeCodeResponse.setOutputList(ouputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        //judgeInfo.setMemory();
        judgeInfo.setTime(maxExecuteTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);


        /* 6. 文件清理 & 资源释放 */
        if (userCodeFile.getParentFile() != null) {
            FileUtil.del(userCodeParentPath);

            System.out.println("文件清理成功！");
        }

        return executeCodeResponse;
    }

    /**
     * 获取错误响应
     *
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示用户程序还没有被执行时，就已经出现异常了。比如：编译错误……
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}


















