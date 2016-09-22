package com.huaban.analysis.jieba.util;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lanxiaowei
 * File操作工具类
 */
public class FileUtils {
    /**获取操作系统名称*/
    private static String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String JAR_PREFIX = "jar:";
    private static final String FILE_PREFIX = "file:";

    /**
     * 根据指定路径查找文件，路径可以是如下各种格式：
     * classpath:xxxxxx.ptoperties
     * jar:/xxx/xxxx.xml
     * file:E:/xxxx/xxxx.dic
     * D:/aaa/bbb.txt
     * /xx/xxxx.txt
     * xxxx/xxxx.xml
     * @param filePath
     * @return
     */
    public static File makeFile(String filePath) {
        if(null == filePath || "".equals(filePath)) {
            return null;
        }
        //明确指定在classpath下查找
        if(filePath.startsWith(CLASSPATH_PREFIX)) {
            filePath = filePath.substring(filePath.indexOf(CLASSPATH_PREFIX) +
                    CLASSPATH_PREFIX.length());
            //去除开头的斜杠
            if(filePath.startsWith("/")){
                filePath = filePath.substring(1);
            }
            return makeFile(filePath,false);
        }
        //明确指定在jar包内部查找
        else if(filePath.startsWith(JAR_PREFIX)) {
            filePath = filePath.substring(filePath.indexOf(JAR_PREFIX) +
                    JAR_PREFIX.length());
            if(!filePath.startsWith("/")){
                filePath = "/" + filePath;
            }
            return makeFile(filePath,true);
        }
        //明确指定在硬盘指定目录下
        else if(filePath.startsWith(FILE_PREFIX)) {
            filePath = filePath.substring(filePath.indexOf(FILE_PREFIX) +
                    FILE_PREFIX.length());
            File file = new File(filePath);
            if(file.exists() && file.canRead()) {
                return file;
            }
            return null;
        }
        //路径以斜杠开头
        else if(filePath.startsWith("/")) {
            //如果是在Windows环境下
            if(isWindows()) {
                //先在jar包内部查找
                File file =  makeFile(filePath,true);
                if(null == file) {
                    //再在Classpath路径下查找
                    filePath = filePath.substring(1);
                    file =  makeFile(filePath,true);
                }
                return file;
            }
            //如果是在Linux环境下
            else {
                File file = new File(filePath);
                if(null != file && file.exists() && file.canRead()) {
                    return file;
                }
                return makeFile(filePath,true);
            }
        }
        //路径非斜杠开头
        else {
            File file = new File(filePath);
            if(null != file && file.exists() && file.canRead()) {
                return file;
            }
            return makeFile(filePath,false);
        }
    }

    public static File makeFile(String filePath,boolean inJar) {
        File file = null;
        String path = null;
        try {
            if(inJar) {
                if(!filePath.startsWith("/")) {
                    filePath = "/" + filePath;
                }
                path = FileUtils.class.getResource(filePath).toURI().getPath();
            } else {
                if(filePath.startsWith("/")) {
                    filePath = filePath.substring(1);
                }
                path = FileUtils.class.getClassLoader().getResource(filePath).toURI().getPath();
            }
            file = new File(path);
            if(file.exists()) {
                path = file.getCanonicalPath();
                return new File(path);
            }
            return null;
        } catch (URISyntaxException e) {
            throw new RuntimeException("load resource from classpath:[" +
                    filePath + "] occur exception,please check out " +
                    "the filePath you input.",e);
        } catch (IOException e) {
            throw new RuntimeException("Create File[" + path +
                    "] occur IO exception.",e);
        }
    }

    /**
     * 获取指定目录下的所有字典文件的路径
     * [不递归查找，只查找指定的当前目录下的所有子文件]
     * @param path  字典目录
     * @return
     */
    public static List<String> getSubFilePath(String path) {
        File file = FileUtils.makeFile(path);
        if(null == file) {
            return null;
        }
        String filePath = FileUtils.normalizePath(file.getPath());
        if(null == filePath) {
            return null;
        }
        file = new File(filePath);
        if(null == file || !file.exists() || !file.canRead()){
            return null;
        }
        List<String> results = new ArrayList<String>();
        //如果是字典目录
        if(file.isDirectory()) {
            //获取当前目录下的所有子文件
            String[] subFile = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".dic");
                }
            });
            if(null != subFile && subFile.length > 0) {
                for(String subFl : subFile) {
                    results.add(subFl);
                }
            }
        } else {
            results.add(filePath);
        }
        return results;
    }

    /**
     * 判断当前是否Windows操作系统
     * @return
     */
    public static boolean isWindows() {
        return OS_NAME.contains("win");
    }

    /**
     * 规范化文件路径
     * @param path
     * @return
     */
    public static String normalizePath(String path) {
        if(null == path || "".equals(path)) {
            return path;
        }
        path = path.replace("\\","/");
        File file = new File(path);
        if(null != file && file.exists() && file.canRead()) {
            try {
                path = file.getCanonicalPath();
                //如果是在Windows环境下
                if(isWindows()) {
                    path = path.replace("\\","/");
                }
                if(!path.endsWith("/")) {
                    path = path + "/";
                }
            } catch (IOException e) {
                path = null;
            }
        } else {
            path = null;
        }
        file = null;
        return path;
    }
}

