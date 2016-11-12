package com.chinacreator.c2.fs.util;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * User: Vurt
 * Date: 13-9-9
 * Time: 下午4:50
 */
public class ContentTypeUtil {
    /**
     * 解析文件类型
     *
     * @param stream   文件内容
     * @param fileName 文件名称
     * @return 文件类型
     * @throws IOException 读取文件时发生错误
     */
    public static MediaType getContentType(BufferedInputStream stream, String fileName) throws IOException {
        Detector detector = new DefaultDetector();
        Metadata tikaMeta = new Metadata();
        tikaMeta.set(Metadata.RESOURCE_NAME_KEY, fileName);
        return detector.detect(stream, tikaMeta);
    }
}
