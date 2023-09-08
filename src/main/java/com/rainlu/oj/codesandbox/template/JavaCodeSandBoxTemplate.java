package com.rainlu.oj.codesandbox.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.rainlu.oj.codesandbox.CodeSandBox;
import com.rainlu.oj.codesandbox.model.ExecuteCodeRequest;
import com.rainlu.oj.codesandbox.model.ExecuteCodeResponse;
import com.rainlu.oj.codesandbox.model.ExecuteMessage;
import com.rainlu.oj.codesandbox.model.JudgeInfo;
import com.rainlu.oj.codesandbox.model.enums.ExecuteStatusEnum;
import com.rainlu.oj.codesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @description Java代码沙箱模板类
 * @author Jun Lu
 */
public abstract class JavaCodeSandBoxTemplate implements CodeSandBox {

    // 将代码文件保存到当前项目根目录下的`tempCode`文件夹中
    private static final String GLOBAL_CODE_PATH_NAME = "tempCode";

    // 文件统一命名
    private static final String STANDARD_CODE_NAME = "Main.java";

    // 超时时间
    private static final long TIME_OUT = 5000L;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        // 1. 保存代码文件
        File userCodeFile = saveCodeFile(code);

        // 2. 编译代码
        ExecuteMessage executeMessage = compileCode(userCodeFile);
        System.out.println(executeMessage);

        // 3. 运行代码
        // 获取所有的程序执行消息
        List<ExecuteMessage> executeMessageList = runCode(inputList, userCodeFile);

        // 4. 整理输出
        ExecuteCodeResponse executeCodeResponse = compactExecuteRespMessage(inputList, executeMessageList);

        // 5. 文件清理
        delFile(userCodeFile);

        return executeCodeResponse;
    }


    /**
     * 1. 把用户的代码保存为文件
     * @param code 用户代码
     * @return
     */
    protected File saveCodeFile(String code) {
        String curProjectRootDir = System.getProperty("user.dir");
        String userCodeParentDirPath = curProjectRootDir + File.separator + GLOBAL_CODE_PATH_NAME;
        if (!FileUtil.exist(userCodeParentDirPath)) {
            FileUtil.mkdir(userCodeParentDirPath);
        }
        // 为每一个用户提交的代码，使用一个不重名的文件夹进行保存
        String randomName4UserCodeDir = userCodeParentDirPath + File.separator + UUID.randomUUID();
        String userCodeFilePath = randomName4UserCodeDir + File.separator + STANDARD_CODE_NAME;

        // 将用户提交的代码写入到`xxxxx/Main.java`文件中
        File userCodeFile = FileUtil.writeString(code, userCodeFilePath, StandardCharsets.UTF_8);

        return userCodeFile;
    }

    /**
     * 2、编译代码
     * @param userCodeFile
     * @return
     */
    protected ExecuteMessage compileCode(File userCodeFile) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            // exec 会开启一个**子进程**
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            // 等待 **子进程**执行结束，以获取返回信息
            //int exitCode = compileProcess.waitFor();

            // 封装程序在执行过程中的消息：控制台上的输出、程序执行时间、占用的内存
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");

            return executeMessage;
        } catch (Exception e) {
            getErrorResponse(e);
        }
        return null;
    }

    /**
     * 3、执行文件，获得执行结果列表
     * @return
     */

    protected List<ExecuteMessage> runCode(List<String> inputList, File userCodeFile) {
        List<ExecuteMessage> executeMessageList = new LinkedList<>();
        try {
            for (String inputArgs : inputList) {
                //                              限定堆内存大小       指定编码      指定类路径   传入参数
                String runCmd = String.format("java -Xmx256m -Dfile.encoding=utf-8 -cp %s Main %s", userCodeFile.getAbsolutePath(), inputArgs);
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 超时控制
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了，中断");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                executeMessageList.add(executeMessage);

                System.out.println(executeMessage);
            }
            return executeMessageList;
        } catch (IOException e) {
            getErrorResponse(e);
        }
        return executeMessageList;
    }


    /**
     * 4、获取输出结果
     * @param executeMessageList
     * @return
     */
    protected ExecuteCodeResponse compactExecuteRespMessage(List<String> inputList, List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();

        List<String> outputList = new LinkedList<>();
        long maxExecuteTime = Long.MIN_VALUE;
        for (ExecuteMessage executeMessage : executeMessageList) {

            Integer exitValue = executeMessage.getExitValue();
            String output = executeMessage.getMessage();
            String errorMessage = executeMessage.getErrorMessage();
            Long time = executeMessage.getTime();
            Long memory = executeMessage.getMemory();

            if (StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                executeCodeResponse.setStatus(ExecuteStatusEnum.RUNTIME_ERROR.getValue());
                executeCodeResponse.setMessage(errorMessage);
                break;
            }

            maxExecuteTime = Math.max(maxExecuteTime, time);
            outputList.add(output);
        }

        if (executeMessageList.size() != inputList.size()) {
            executeCodeResponse.setStatus(ExecuteStatusEnum.CODE_ERROR.getValue());
        }

        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxExecuteTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        return executeCodeResponse;
    }


    /**
     * 5、删除文件
     * @param userCodeFile
     * @return
     */
    protected void delFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeFile.getParentFile().getAbsolutePath());

            System.out.println("文件清理" + (del ? "成功" : "失败") + "！");
        }
    }

    /**
     * 6. 异常|错误输出统一处理
     *
     * @param e
     * @return
     */
    protected ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示用户程序还没有被执行时，就已经出现异常了。比如：编译错误……
        executeCodeResponse.setStatus(ExecuteStatusEnum.COMPILE_ERROR.getValue());
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
