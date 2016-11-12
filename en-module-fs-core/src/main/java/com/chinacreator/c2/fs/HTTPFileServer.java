package com.chinacreator.c2.fs;

import com.chinacreator.c2.fs.exception.FileNotExsitException;

/**
 * 支持通过HTTP访问的文件存储服务
 * @author Vurt
 *
 */
public interface HTTPFileServer extends FileServer{
	/**
	 * 获取文件通过Http协议访问的URL，相对路径，不要以/开头
	 * @param path 文件的定位字符串(PATH字符串，不同服务实现可能会有不同的形式)
	 * @return 文件的访问url(文件访问路径，不包括控制器的url前缀，比如最后的访问路径为/file/a/b/c.jpg，文件访问控制器的路径前缀为/file/，那么这里会返回a/b/c.jpg)
	 * @throws FileNotExsitException 文件不存在
	 */
	public String getUrl(String path) throws Exception;
	
	/**
	 * 将请求文件的URL解析成文件服务支持的PATH(文件的url跟文件存储服务中的PATH可能会不一样)
	 * @param urlPath url路径，不会包括文件服务的Web Controller的访问路径前缀(比如/file/)
	 * @return 文件服务内部支持的文件PATH
	 */
	public String parsePath(String urlPath) throws Exception;
}
