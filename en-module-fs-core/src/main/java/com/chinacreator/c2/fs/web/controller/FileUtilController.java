package com.chinacreator.c2.fs.web.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.chinacreator.c2.fs.FileMetadata;
import com.chinacreator.c2.fs.HTTPFileServer;
import com.chinacreator.c2.fs.exception.FileNotExsitException;
import com.chinacreator.c2.fs.exception.InvalidFilePathException;
import com.chinacreator.c2.fs.web.Constants;
import com.chinacreator.c2.fs.web.Util;
import com.chinacreator.c2.web.controller.ResponseFactory;

@Controller
public class FileUtilController {
	 
	/**
	 * 检查文件是否存在
	 * 
	 * @return json字符串,{exist:true|false}
	 */
	@RequestMapping(value=Constants.URL_UTIL_DOWNLOAD_PREFIX+"**",method = RequestMethod.GET)
	public Object doDownload(HttpServletRequest request, HttpServletResponse response){
		
		// download
		try {
			
			HTTPFileServer server = Util.getFileServer();
			
			String path = server.parsePath(Util.removeUrlPrefix(request,Constants.URL_UTIL_DOWNLOAD_PREFIX));
			
			if (StringUtils.isEmpty(path)) {
				throw new InvalidFilePathException("文件路径不能为空!");
			}
			InputStream inputStream = server.get(path);
			if (inputStream == null) {
				throw new FileNotExsitException(path);
			}
			FileMetadata metadata = server.getMetaData(path);

			response.addHeader("Content-Disposition", "attachment; filename="
					+ java.net.URLEncoder.encode(metadata.getName(), "UTF-8"));
			response.setContentType("application/octet-stream");
			response.setContentLength(metadata.getFilesize().intValue());

			OutputStream stream = response.getOutputStream();

			byte[] buffer = new byte[1024*8];
			int length=0,buffred=0;
			int maxBufferSize=1024*1024;//最多缓存1M数据
			while ((length = inputStream.read(buffer)) != -1) {
				stream.write(buffer, 0, length);
				buffred += length;
		        if (buffred > maxBufferSize) { 
		        	buffred = 0;
		        	stream.flush();
		        }
			}

			stream.flush();
			stream.close();
			inputStream.close();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseFactory()
					.createResponseBodyHtml("<script type=\"text/javascript\">alert('"
							+ e.getMessage() + "');</script>");
		}
	}
	
	
	/**
	 * 检查文件是否存在
	 * 
	 * @return json字符串,{exist:true|false}
	 */
	@RequestMapping(value=Constants.URL_UTIL_CHECK_PREFIX+"**",method = RequestMethod.GET)
	public Object checkExist(HttpServletRequest request, HttpServletResponse response){
		try{
			HTTPFileServer server = Util.getFileServer();
			String path = server.parsePath(Util.removeUrlPrefix(request,Constants.URL_UTIL_CHECK_PREFIX));
			HashMap<String, Boolean> param = new HashMap<String, Boolean>();
			param.put("exist", server.exsits(path));
			return new ResponseFactory().createResponseBodyJSONObject(param);
		}catch(Exception e){
			e.printStackTrace();
			return new ResponseFactory().createResponseBodyException(e);
		}
	}
}
