package com.chinacreator.c2.fs.inner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * 简单文件系统内的文件读取流，用于区分外界的流和SimpleFS内部的流
 * User: Vurt
 * Date: 13-11-11
 * Time: 下午5:23
 */
public class SimpleFSInputStream extends FileInputStream {
    private String id;

    public SimpleFSInputStream(String id, File file) throws FileNotFoundException {
        super(file);
        this.id = id;
    }

    /**
     * 文件id
     */
    public String getId() {
        return id;
    }
}
