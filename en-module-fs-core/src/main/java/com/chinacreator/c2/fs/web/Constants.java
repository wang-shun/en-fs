package com.chinacreator.c2.fs.web;

/**
 * 文件上传下载的一些常量
 * @author Vurt
 */
public interface Constants {
	/**
	 * 文件访问路径，包括上传和访问
	 */
	String URL_FILE_PREFIX="file/";
	
	/**
	 * 文件服务的一些辅助功能REST接口路径
	 */
	String URL_FILE_UTIL_PREFIX="fileutil/";
	
	/**
	 * 专门用于文件下载的url前缀<p>
	 * 浏览器在处理一些类型的响应时，会直接把内容显示在浏览器中，比如html，各类图片等，
	 * 通过该URL访问的所有的内容都会被直接响应为application/octet-stream类型，也就是说浏览器会直接触发下载动作
	 */
	String URL_UTIL_DOWNLOAD_PREFIX= URL_FILE_UTIL_PREFIX+"download/";
	
	/**
	 * 检查文件是否存在的URL
	 */
	String URL_UTIL_CHECK_PREFIX= URL_FILE_UTIL_PREFIX+"check/";
	
}
