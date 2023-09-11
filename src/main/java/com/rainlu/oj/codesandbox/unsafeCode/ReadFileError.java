package com.rainlu.oj.codesandbox.unsafeCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @description 恶意读取服务器文件（文件信息泄露）
 * @author Jun Lu
 */
public class ReadFileError {

    public static void main(String[] args) throws InterruptedException, IOException {
        String userDir = System.getProperty("user.dir");
        // 利用相对路径恶意读取服务器文件
        String filePath = userDir + File.separator + "src/main/resources/application.yml";
        List<String> allLines = Files.readAllLines(Paths.get(filePath));
        System.out.println(String.join("\n", allLines));
    }

}
