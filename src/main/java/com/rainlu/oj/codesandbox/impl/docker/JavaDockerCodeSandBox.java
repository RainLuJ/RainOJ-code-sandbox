package com.rainlu.oj.codesandbox.impl.docker;

import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.rainlu.oj.codesandbox.model.ExecuteMessage;
import com.rainlu.oj.codesandbox.template.JavaCodeSandBoxTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JavaDockerCodeSandBox extends JavaCodeSandBoxTemplate {

    private static final long TIME_OUT = 5000L;

    private static final Boolean FIRST_INIT = true;


    /**
     * 3、创建容器，使用容器来执行代码
     * @param userCodeFile
     * @param inputList
     * @return
     */
    @Override
    protected List<ExecuteMessage> runCode(List<String> inputList, File userCodeFile) {

        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        // 获取默认的 Docker Client
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();

        // 1. 拉取镜像
        String image = "openjdk:8-alpine";
        if (FIRST_INIT) {
            // docker pull openjdk:8-alpine  （未按回车执行）
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            // 回调函数：接收命令执行后的一系列消息
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像：" + item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd
                        // 执行  （按下回车）
                        .exec(pullImageResultCallback)
                        // 等待执行完毕（必须！！！否则无法接收到完整的执行信息）
                        .awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
        }

        System.out.println("下载完成");

        /* 2. 创建容器 */
        // 2.1 获取创建容器的命令
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        // Docker配置项
        HostConfig hostConfig = new HostConfig();
        // 限制程序运行能占用的内存
        hostConfig.withMemory(100 * 1000 * 1000L);
        // 限制内存交换的容积
        hostConfig.withMemorySwap(0L);
        // 限制能使用的CPU核心
        hostConfig.withCpuCount(1L);
        // 安全管理配置
        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
        // 容器目录挂载。当前用户提交的程序保存在服务器上的目录(userCodeParentPath)  <==>  容器中的目录(/app)
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        // 2.2 在创建容器时绑定参数配置
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                // 禁用网络
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
                // 将容器的标准输入、输出、错误输出都与宿主机进行绑定
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                // docker -it：交互式运行
                .withTty(true)
                .exec();
        System.out.println(createContainerResponse);
        // 2.3 获取所创建的容器id
        String containerId = createContainerResponse.getId();

        // 2.4 启动所创建的容器
        dockerClient.startContainerCmd(containerId).exec();

        /* 3. 使用容器执行java程序：docker exec keen_blackwell java -cp /app Main 1 3 */
        // 执行命令并获取结果
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            // 需要将参数按照空格拆分为数组进行传递，否则会被识别成一个字符串参数
            String[] inputArgsArray = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
            // 3.1 docker exec keen_blackwell java -cp /app Main 1 3：创建使用容器执行程序的命令
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    // 将容器的标准输入、输出、错误输出都与宿主机进行绑定
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("创建执行命令：" + execCreateCmdResponse);

            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] message = {null};
            final String[] errorMessage = {null};
            long time = 0L;
            // 判断是否超时
            final boolean[] timeout = {true};
            String execId = execCreateCmdResponse.getId();
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    // 如果执行完成，则表示没超时
                    timeout[0] = false;
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessage[0] = new String(frame.getPayload());
                        System.out.println("输出错误结果：" + errorMessage[0]);
                    } else {
                        message[0] = new String(frame.getPayload());
                        System.out.println("输出结果：" + message[0]);
                    }
                    super.onNext(frame);
                }
            };

            final long[] maxMemory = {0L};

            // 获取占用的内存：docker stats
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }

                @Override
                public void close() throws IOException {

                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }
            });
            statsCmd.exec(statisticsResultCallback);
            try {
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                statsCmd.close();
            } catch (InterruptedException e) {
                System.out.println("程序执行异常");
                throw new RuntimeException(e);
            }
            executeMessage.setMessage(message[0]);
            executeMessage.setErrorMessage(timeout[0] ? errorMessage[0] = "运行超时！" : errorMessage[0]);
            executeMessage.setTime(time);
            executeMessage.setMemory(maxMemory[0]);
            executeMessageList.add(executeMessage);
        }
        return executeMessageList;
    }
}


















