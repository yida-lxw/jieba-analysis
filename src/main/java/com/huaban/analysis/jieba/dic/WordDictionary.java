package com.huaban.analysis.jieba.dic;

import com.huaban.analysis.jieba.conf.Configuration;
import com.huaban.analysis.jieba.conf.DefaultConfiguration;
import com.huaban.analysis.jieba.util.FileUtils;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

import static com.huaban.analysis.jieba.util.FileUtils.getSubFilePath;

public class WordDictionary {
    private static WordDictionary singleton;
    private static final String MAIN_DICT = "dict.txt";
    private static String USER_DICT_SUFFIX = ".dic";

    public final Map<String, Double> freqs = new HashMap<String, Double>();
    public final Set<String> loadedPath = new HashSet<String>();
    /**停用词*/
    private Set<String> stopwords = new HashSet<String>();
    private Double minFreq = Double.MAX_VALUE;
    private Double total = 0.0;
    private DictSegment _dict;
    private Configuration config;

    private WordDictionary() {
        this.config = DefaultConfiguration.getInstance();
        //加载核心字典
        this.loadMainDict();
        //加载用户自定义扩展字典
        this.loadUserDictByPath(config.userDicPath());
        //加载用户停用词字典
        this.loadStopwordDictByPath(config.stopwordPath());
    }

    public static WordDictionary getInstance() {
        if (singleton == null) {
            synchronized (WordDictionary.class) {
                if (singleton == null) {
                    singleton = new WordDictionary();
                    return singleton;
                }
            }
        }
        return singleton;
    }

    /**
     * for ES to initialize the user dictionary.
     * 
     * @param configFile
     */
    public void init(Path configFile) {
        String abspath = configFile.toAbsolutePath().toString();
        System.out.println("initialize user dictionary:" + abspath);
        synchronized (WordDictionary.class) {
            if (loadedPath.contains(abspath)) {
                return;
            }
            DirectoryStream<Path> stream;
            try {
                stream = Files.newDirectoryStream(configFile, String.format(Locale.getDefault(), "*%s", USER_DICT_SUFFIX));
                for (Path path: stream){
                    singleton.loadUserDict(path,StandardCharsets.UTF_8.displayName());
                }
                loadedPath.add(abspath);
            } catch (IOException e) {
                throw new RuntimeException(String.format(Locale.getDefault(), "%s: load user dict failure!", configFile.toString()),e);
            }
        }
    }
    
    
    /**
     * 重置字典，方便用户使用自定义扩展字典，放弃使用内置的核心字典
     */
    public void resetDict(){
    	_dict = new DictSegment((char) 0);
    	freqs.clear();
    }

    /**
     * 加载核心字典
     */
    public void loadMainDict() {
        InputStream is = loadDictInputStream();
        if(null == is) {
            throw new RuntimeException("Main dictionary[dict.txt] not found.");
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            long s = System.currentTimeMillis();
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");

                if (tokens.length < 2) {
                    continue;
                }
                String word = tokens[0];
                double freq = Double.valueOf(tokens[1]);
                total += freq;
                word = addWord(word);
                freqs.put(word, freq);
            }
            // normalize
            for (Entry<String, Double> entry : freqs.entrySet()) {
                entry.setValue((Math.log(entry.getValue() / total)));
                minFreq = Math.min(entry.getValue(), minFreq);
            }
            System.out.println(String.format(Locale.getDefault(), "main dict load finished, time elapsed %d ms",
                    System.currentTimeMillis() - s));
        } catch (IOException e) {
            throw new RuntimeException(String.format(Locale.getDefault(), "%s load failure!", MAIN_DICT),e);
        } catch (NullPointerException e) {
            throw new RuntimeException("InputStream about Main dictionary[dict.txt] is null.",e);
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            }
            catch (IOException e) {
                System.err.println(String.format(Locale.getDefault(), "%s close failure!", MAIN_DICT));
            }
        }
    }

    public InputStream loadDictInputStream() {
        _dict = new DictSegment((char) 0);
        InputStream is = this.getClass().getResourceAsStream("/" + MAIN_DICT);
        if(null == is) {
            is = this.getClass().getClassLoader().getResourceAsStream(MAIN_DICT);
            if(null == is) {
                try {
                    is = new FileInputStream(MAIN_DICT);
                } catch (FileNotFoundException e) {
                    return null;
                }
            }
        }
        return is;
    }


    /**
     * 添加一个新词
     * @param word
     * @return
     */
    public String addWord(String word) {
        if (null != word && !"".equals(word.trim())) {
            String key = word.trim().toLowerCase(Locale.getDefault());
            _dict.fillSegment(key.toCharArray());
            return key;
        }
        return null;
    }

    public void loadUserDict(Path userDict) {
        loadUserDict(userDict, StandardCharsets.UTF_8);
    }

    public void loadUserDict(Path userDict,String charset) {
        loadUserDict(userDict, Charset.forName(charset));
    }

    public void loadUserDict(Path userDict, Charset charset) {
        loadUserDict(userDict.toString(),charset.displayName());
    }

    /**
     * 加载用户自定义扩展字典
     * @param userDict
     */
    public void loadUserDict(String userDict,Charset charset) {
        loadUserDict(Paths.get(userDict), charset);
    }

    /**
     * 加载用户自定义扩展字典
     * @param userDict
     */
    public void loadUserDict(String userDict, String charset) {
        loadUserDict(userDict,charset,false);
    }

    /**
     * 加载用户自定义扩展字典
     * @param userDict
     */
    public void loadUserDict(String userDict, String charset,boolean absolutePath) {
        if(null == userDict || "".equals(userDict)) {
            throw new RuntimeException("User custom dictionary file path is null.");
        }
        BufferedReader br = null;
        try {
            br = createReader(userDict,charset,absolutePath);
            long s = System.currentTimeMillis();
            int count = 0;
            while (br.ready()) {
                String line = br.readLine();
                //忽略空白行
                if(line.length() <= 0 || line.matches("^$")) {
                    continue;
                }
                //忽略注释行
                if(line.length() > 1 && line.startsWith("#")) {
                    continue;
                }
                String[] tokens = line.split("[\t ]+");
                if (tokens.length < 1) {
                    // Ignore empty line
                    continue;
                }
                String word = tokens[0];
                double freq = 3.0d;
                if (tokens.length == 2) {
                    freq = Double.valueOf(tokens[1]);
                }
                word = addWord(word); 
                freqs.put(word, Math.log(freq / total));
                count++;
            }
            System.out.println(String.format(Locale.getDefault(), "user dict %s load finished, tot words:%d, time elapsed:%dms", userDict.toString(), count, System.currentTimeMillis() - s));
        } catch (IOException e) {
            throw new RuntimeException(String.format(Locale.getDefault(),
                    "%s: load user dict failure!", userDict),e);
        } finally {
            try {
                if(null != br) {
                    br.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Close BufferedReader occur exception.",e);
            }
        }
    }

    /**
     * 加载用户自定义停用词字典
     * @param stopwordDict
     */
    public void loadStopwordDict(String stopwordDict) {
        loadStopwordDict(stopwordDict,StandardCharsets.UTF_8.displayName(),false);
    }

    /**
     * 加载用户自定义停用词字典
     * @param stopwordDict
     */
    public void loadStopwordDict(String stopwordDict, String charset) {
        loadStopwordDict(stopwordDict,charset,false);
    }

    /**
     * 加载用户自定义停用词字典
     * @param stopwordDict
     */
    public void loadStopwordDict(String stopwordDict, String charset,boolean absolutePath) {
        if(null == stopwordDict || "".equals(stopwordDict)) {
            throw new RuntimeException("Stop word dictionary file path is null.");
        }
        BufferedReader br = null;
        try {
            br = createReader(stopwordDict,charset,absolutePath);
            long s = System.currentTimeMillis();
            int count = 0;
            while (br.ready()) {
                String line = br.readLine();
                //忽略空白行
                if(null == line || line.length() <= 0 || line.matches("^$")) {
                    continue;
                }
                //忽略注释行
                if(line.length() > 1 && line.startsWith("#")) {
                    continue;
                }
                this.stopwords.add(line.trim());
                count++;
            }
            System.out.println(String.format(Locale.getDefault(),
                    "stopword dict %s load finished, tot words:%d, time elapsed:%dms",
                    stopwordDict.toString(), count, System.currentTimeMillis() - s));
        } catch (IOException e) {
            throw new RuntimeException(String.format(Locale.getDefault(),
                    "%s: load stopword dict failure!", stopwordDict.toString()),e);
        } finally {
            try {
                if(null != br) {
                    br.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Close BufferedReader occur exception.",e);
            }
        }
    }

    /**
     * 加载用户自定义扩展字典[通过指定一个字典目录批量加载]
     * @param userDictPath
     */
    public void loadUserDictByPath(String userDictPath, String charset) {
        List<String> filePaths = getSubFilePath(userDictPath);
        if(null == filePaths || filePaths.size() <= 0) {
            return;
        }
        for(String dicPath : filePaths) {
            loadUserDict(dicPath,charset,true);
        }
    }

    /**
     * 加载用户自定义扩展字典[通过指定多个字典文件路径或多个字典目录批量加载]
     * @param userDictPaths
     * @param charset
     */
    public void loadUserDictByPath(List<String> userDictPaths, String charset) {
        if(null == userDictPaths || userDictPaths.size() <= 0) {
            return;
        }
        List<String> filePaths = new ArrayList<String>();
        for(String path : userDictPaths) {
            List<String> paths = FileUtils.getSubFilePath(path);
            if(null == paths || paths.size() <= 0) {
                continue;
            }
            filePaths.addAll(paths);
        }
        if(null == filePaths || filePaths.size() <= 0) {
            return;
        }
        for(String dicPath : filePaths) {
            loadUserDict(dicPath,charset,true);
        }
    }

    /**
     * 加载用户自定义扩展字典[通过指定多个字典文件路径或多个字典目录批量加载]
     * @param userDictPaths
     */
    public void loadUserDictByPath(List<String> userDictPaths) {
        loadUserDictByPath(userDictPaths,StandardCharsets.UTF_8.displayName());
    }

    /**
     * 加载用户自定义停用词字典[通过指定一个字典目录批量加载]
     * @param stopwordDictPath
     * @param charset
     */
    public void loadStopwordDictByPath(String stopwordDictPath, String charset) {
        List<String> filePaths = getSubFilePath(stopwordDictPath);
        if(null == filePaths || filePaths.size() <= 0) {
            return;
        }
        for(String dicPath : filePaths) {
            loadStopwordDict(dicPath,charset,true);
        }
    }

    /**
     * 加载用户自定义停用词字典[通过指定多个字典文件路径或多个字典目录批量加载]
     * @param stopwordDictPaths
     * @param charset
     */
    public void loadStopwordDictByPath(List<String> stopwordDictPaths, String charset) {
        if(null == stopwordDictPaths || stopwordDictPaths.size() <= 0) {
            return;
        }
        List<String> filePaths = new ArrayList<String>();
        for(String path : stopwordDictPaths) {
            List<String> paths = FileUtils.getSubFilePath(path);
            if(null == paths || paths.size() <= 0) {
                continue;
            }
            filePaths.addAll(paths);
        }
        if(null == filePaths || filePaths.size() <= 0) {
            return;
        }
        for(String dicPath : filePaths) {
            loadStopwordDict(dicPath,charset,true);
        }
    }

    /**
     * 加载用户自定义停用词字典[通过指定多个字典文件路径或多个字典目录批量加载]
     * @param stopwordDictPaths
     */
    public void loadStopwordDictByPath(List<String> stopwordDictPaths) {
        loadStopwordDictByPath(stopwordDictPaths,StandardCharsets.UTF_8.displayName());
    }

    /**
     * 创建字典文件输入流
     * @param dicPath  字典文件的加载路径
     * @return
     */
    public BufferedReader createReader(String dicPath) {
        return createReader(dicPath,StandardCharsets.UTF_8.displayName());
    }

    /**
     * 创建字典文件输入流
     * @param dicPath  字典文件的加载路径
     * @param charset  字典文件的字符编码
     * @return
     */
    public BufferedReader createReader(String dicPath,String charset) {
        BufferedReader br = null;
        if(dicPath.indexOf("/") == -1 && dicPath.indexOf("\\") != -1) {
            dicPath = dicPath.replace("\\","/");
        }
        try {
            if(!dicPath.startsWith("/")) {
                dicPath = "/" + dicPath;
            }
            InputStream is = this.getClass().getResourceAsStream(dicPath);
            if(null == is) {
                if(dicPath.startsWith("/")) {
                    dicPath = dicPath.substring(1);
                }
                is = this.getClass().getClassLoader().getResourceAsStream(dicPath);
                if(null == is) {
                    try {
                        is = new FileInputStream(dicPath);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("Dictionary file[" + dicPath + "] not found.",e);
                    }
                }
            }
            if(null == is) {
                throw new RuntimeException("Dictionary[" + dicPath + "] not found.");
            }
            br = new BufferedReader(new InputStreamReader(is, charset));
        } catch (IOException e) {
            throw new RuntimeException(String.format(Locale.getDefault(), "%s: load dictionary file failure!",
                    dicPath));
        } finally {
            return br;
        }
    }

    public BufferedReader createReader(String dicPath,String charset,boolean absolutePath) {
        if(absolutePath){
            try {
                InputStream is = new FileInputStream(dicPath);
                return new BufferedReader(new InputStreamReader(is, charset));
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Dictionary[" + dicPath + "] not found.");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Dictionary file[" + dicPath + "]'s encoding unsupported");
            }
        } else {
            return createReader(dicPath,charset);
        }
    }

    public DictSegment getTrie() {
        return this._dict;
    }


    public boolean containsWord(String word) {
        return freqs.containsKey(word);
    }

    /**
     * 判断一个词是不是停用词
     * @param word
     * @return
     */
    public boolean isStopword(String word) {
        if(null == word || "".equals(word)) {
            return true;
        }
        if(null == this.stopwords || this.stopwords.size() <= 0) {
            return false;
        }
        return this.stopwords.contains(word);
    }

    public Double getFreq(String key) {
        if (containsWord(key)) {
            return freqs.get(key);
        }
        return minFreq;
    }

    public Set<String> getStopwords() {
        return stopwords;
    }
}
