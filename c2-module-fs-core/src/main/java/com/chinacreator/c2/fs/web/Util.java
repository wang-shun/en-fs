package com.chinacreator.c2.fs.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.chinacreator.c2.fs.HTTPFileServer;
import com.chinacreator.c2.fs.exception.InvalidFilePathException;
import com.chinacreator.c2.ioc.ApplicationContextManager;

public class Util {
	private static HTTPFileServer fileServer;

	public static HTTPFileServer getFileServer() {
		if (fileServer == null) {
			fileServer = ApplicationContextManager.getContext().getBean(
					"fileServer", HTTPFileServer.class);
		}
		return fileServer;
	}

	/**
	 * 获取url中的文件访问路径
	 * 
	 * @param request
	 * @return
	 */
	public static String removeUrlPrefix(HttpServletRequest request,String prefix) {
		if(StringUtils.isEmpty(prefix)){
			throw new InvalidFilePathException("文件url前缀不能为空");
		}
		String uri = request.getRequestURI();
		int index = uri.indexOf(prefix);
		if(index<0){
			throw new InvalidFilePathException("无效的请求文件路径："+uri);
		}
		return uri.substring(index + prefix.length());
	}
}
