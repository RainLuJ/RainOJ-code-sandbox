package com.rainlu.oj.codesandbox.impl.interact;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.rainlu.oj.codesandbox.CodeSandBox;
import com.rainlu.oj.codesandbox.model.ExecuteCodeRequest;
import com.rainlu.oj.codesandbox.model.ExecuteCodeResponse;
import com.rainlu.oj.codesandbox.model.ExecuteMessage;
import com.rainlu.oj.codesandbox.utils.ProcessUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JavaNativeCodeSandbox implements CodeSandBox {

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
            e.printStackTrace();
        }

        /* 4. 运行"Main.java"文件 */
        // 将输入用例传入
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Dfile.encoding=utf-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                System.out.println(executeMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
