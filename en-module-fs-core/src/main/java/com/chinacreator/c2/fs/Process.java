package com.chinacreator.c2.fs;

import java.util.List;
import java.util.Map;

import com.chinacreator.c2.fs.web.Result;

/**
 * 文件上传处理接口
 * @author hushowly
 * 
 */
public interface Process {
	
	/**
	 * 文件上传处理，支持多文件
	 * @param fileMap   上传待处理文件列表
	 * @param map    request请求参数
	 * @return          处理文件结果
	*/
	public  Result processUpload(List<FileInput> fileList,Map<String, String[]> map) throws Exception;
	
	
	/**
	 * 处理文件下载
	 * @param fm
	 * @param map
	 * @return
	 */
	public  DownResult processDown(String fpath,Map<String, String[]> map) throws Exception;
	
	
	/**
	 * 删除上传文件
	 * @param fm
	 * @param map
	 * @return
	 */
	public  boolean processDelete(String fpath,Map<String, String[]> map) throws Exception;
	
	
	/**
	 * 获取文件是否存在
	 * @param fm
	 * @param map
	 * @return
	 */
	public  boolean exist(String fpath,Map<String, String[]> map) throws Exception;
	
	

	
}