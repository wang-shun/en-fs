package com.chinacreator.c2.fs.dir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 目录文件服务器统一接口
 * @author hushowly
 */
public interface DirFileServer {

	/**
	 * 判断目录文件是否存在
	 * @param path  文件路径
	 * @return      是否存在boolean值
	 */
	public boolean exsits(String relativePath) throws Exception;
	
	/**
	 * 获取目录文件
	 * @param path  文件路径
	 * @return      返回文件
	 */
	public File getFile(String relativePath);
	
	/**
	 * 向目录服务器添加文件
	 * @param content  文件流
	 * @param path     文件路径
	 * @return         返回该添加的文件
	 * @throws IOException
	 */
	public DirFile add(InputStream content,String relativePath)
			throws Exception;

	/**
	 * 删除文件
	 * @param path  文件路径
	 * @return
	 */
	public boolean delete(String relativePath);
	
	
	/**
	 * 目录文件重命名
	 * @param path  文件路径
	 * @return
	 */
	public boolean reName(String relativePath,String newName);

	
	/**
	 * 以流的方式获取文件
	 * @param path   文件路径 
	 * @return
	 * @throws IOException
	 */
	public InputStream getFileStream(String relativePath) throws IOException;
	
}
