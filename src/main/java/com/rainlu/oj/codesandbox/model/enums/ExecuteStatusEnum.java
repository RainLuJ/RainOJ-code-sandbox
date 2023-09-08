package com.rainlu.oj.codesandbox.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description 判题信息的消息枚举
 * @author Jun Lu
 * @date 2023-09-03 18:47:47
 */
public enum ExecuteStatusEnum {

    CODE_ERROR("代码未AC", 1),
    COMPILE_ERROR("代码编译错误", 2),
    RUNTIME_ERROR("运行异常", 3)
    ;

    private final String text;

    private final Integer value;

    ExecuteStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取所有定义的value
     */
    public static List<Integer> getValues() {
        // values()：获取当前枚举类中的所有枚举对象
        //                                取出枚举对象中的value属性
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     */
    public static ExecuteStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }

        for (ExecuteStatusEnum anEnum : ExecuteStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

}
