package com.huaban.analysis.jieba.conf;

import java.util.List;

/**
 * Created by Lanxiaowei
 * Jieba分词器配置管理接口
 */
public interface Configuration {
    /**
     * 返回分词模式
     * @return
     */
    public String mode();

    /**
     * 模型文件的加载路径
     * @return
     */
    public String modelPath();

    /**
     * 返回停用词字典文件的加载路径
     * @return
     */
    public List<String> stopwordPath();

    /**
     * 返回用户自定义扩展字典文件的加载路径
     * @return
     */
    public List<String> userDicPath();
}
