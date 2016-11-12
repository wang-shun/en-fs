package com.chinacreator.c2.fs.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chinacreator.c2.fs.FileMetadata;
import com.chinacreator.c2.fs.HTTPFileServer;
import com.chinacreator.c2.fs.exception.FileNotExsitException;
import com.chinacreator.c2.fs.exception.InvalidFilePathException;
import com.chinacreator.c2.fs.util.Constants.HttpType;
import com.chinacreator.c2.fs.web.Constants;
import com.chinacreator.c2.fs.web.FileUploadResult;
import com.chinacreator.c2.fs.web.Util;
import com.chinacreator.c2.web.controller.ResponseFactory;

/**
 * 处理文件上传和访问请求的控制器
 */
@Controller
@RequestMapping(Constants.URL_FILE_PREFIX)
public class FileController {
	/**
	 * 获取附件，返回相应类型流，会被浏览器自动识别
	 * 
	 * @param id
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="**",method = RequestMethod.GET)
	public Object getFile(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		HTTPFileServer server = Util.getFileServer();
		String path = server.parsePath(Util.removeUrlPrefix(request,
				Constants.URL_FILE_PREFIX));
		
		try{
		InputStream in = server.get(path);
		OutputStream out = response.getOutputStream();
			try {
				if (StringUtils.isEmpty(path)) {
					throw new InvalidFilePathException("文件路径不能为空!");
				}
				
				FileMetadata metadata = server.getMetaData(path);
	
				response.setContentType(metadata.getMimetype());
				response.setContentLength(metadata.getFilesize().intValue());
				response.addHeader( "Content-Disposition" ,  "attachment; filename="  + java.net.URLEncoder.encode(metadata.getName(), "UTF-8"));
				
				byte[] buffer = new byte[1024*8];
				int length=0,buffred=0;
				int maxBufferSize=1024*1024;//最多缓存1M数据
				while ((length = in.read(buffer)) != -1) {
					out.write(buffer, 0, length);
					buffred += length;
			        if (buffred > maxBufferSize) { 
			        	buffred = 0;
			            out.flush();
			        }
				}
				
				return null;
			} catch (IOException e) {
				return new ResponseFactory()
						.createResponseBodyHtml("<script type=\"text/javascript\">alert('"
								+ e.getMessage() + "');</script>");
			}finally{
				//避免最后一段读完没有flush
				out.flush();
				
				IOUtils.closeQuietly(in);
			    IOUtils.closeQuietly(out);
			}
		}catch(FileNotExsitException e){
			return new ResponseFactory()
					.createResponseBodyHtml("<script type=\"text/javascript\">alert('"
							+ e.getMessage() + "');</script>");
		}catch (Exception e) {
			return new ResponseFactory().createResponseBodyException(e);
		}
	}

	/**
	 * 上传文件
	 * 
	 * @param @param file
	 * @param @return
	 * @param @throws IOException
	 * @return Object
	 */
	@RequestMapping(value="**",method = RequestMethod.POST)
	public Object uploadFile(MultipartHttpServletRequest multipartRequest)
			throws IOException {

		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		
		InputStream is=null;
		
		try {
			
			Iterator<String> it=fileMap.keySet().iterator();
			if(!it.hasNext())  throw new Exception("上传附件不能为空！"); 
			
			MultipartFile file=fileMap.get(it.next());
			
			HTTPFileServer server = Util.getFileServer();
			String path = "";

			FileMetadata meta = null;
			is= file.getInputStream();
			
			// 用摘要+长度在库中查询、判断文件是否已经存在,若存在则更新元数据id,否则新增
			//if (server instanceof LinkableFileServer) {
			//	LinkableFileServer linkableFileServer = (LinkableFileServer) server;
			//	if ((meta = linkableFileServer.contains(digest = SummaryBuilder
			//			.getFileSummaryByMD5(file.getInputStream())
			//			+ file.getSize())) != null) {
			//		path = linkableFileServer.link(meta);
			//	}
			//}
			// 存储服务不是linkable或者不存在能link的文件

			meta = new FileMetadata();
			meta.setName(file.getOriginalFilename());
			meta.setMimetype(file.getContentType());
			meta.setFilesize(file.getSize());
			path = server.add(is, meta).getId();
			
			// 访问路径为file/文件id/文件名
			String url = Constants.URL_FILE_PREFIX + server.getUrl(path);
			return new ResponseFactory()
					.createResponseBodyJSONObject(new FileUploadResult(HttpType.SUCCESS.ordinal(),"成功",meta.getName(),path,url));
		} catch (Exception e) {
			return new ResponseFactory().createResponseBodyException(e);
		} finally {
			if (is != null)
				is.close();
		}
	}
	
	
    /**
     * 文件批量上传
     * @param request   请求对象
     * @param id        控件id
     * @param callback  上传成功回调函数
     * @return Object   返回回调js
     * @throws IOException
     */
    @RequestMapping(value="batch",method = RequestMethod.POST)
    public Object uploadBatchFile(HttpServletRequest request,@RequestParam("controlId") String controlId)
            throws IOException {
    	
    	try{
    		
    		HTTPFileServer server = Util.getFileServer();
        	MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        			
        	//文件列表
        	Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        	LinkedList<FileMetadata> resultList=new LinkedList<FileMetadata>();
        	
        	//验证文件大小
        	List<Object> fileList=new ArrayList<Object>();
        	String maxSizeStr=request.getParameter("maxSize");
        	if(StringUtils.isNotEmpty(maxSizeStr)&&StringUtils.isNumeric(maxSizeStr)){
            	for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            		CommonsMultipartFile file = (CommonsMultipartFile) entity.getValue();
            		int maxSize=Integer.parseInt(maxSizeStr);
            		if(file.getSize()>maxSize){
            			FileMetadata meta = new FileMetadata();
            			meta.setFilesize(file.getSize());
            			meta.setName(file.getName());
            			meta.setMimetype(file.getContentType());
            			fileList.add(meta);
            		}
            	}
        	}
        	
        	if(fileList.size()>0){
        		HashMap<String,Object> reData=new HashMap<String, Object>();
        		reData.put("errorMsg","单个文件大小超过限制！");
        		reData.put("files",fileList);
        		
        		HashMap<String,Object> d=new HashMap<String, Object>();
        		d.put("data",reData);
        		String jsonStr=JSON.toJSONString(d,SerializerFeature.DisableCircularReferenceDetect);
        		return new ResponseFactory().createResponseBodyHtml("<script type=\"text/javascript\">" +
        				"var scope=parent.angular.element(\"#"+controlId+"\").scope();" +
        				"scope.$view."+controlId+".$scope.errorCallback("+jsonStr+");</script>");
        	}

        	
    		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
    			
    			CommonsMultipartFile file = (CommonsMultipartFile) entity.getValue();
    			System.out.println("处理上传文件：" + file.getOriginalFilename());
    			String digest, result = "";
    			FileMetadata meta = null;
    			InputStream is = file.getInputStream();
    			// 用摘要+长度在库中查询、判断文件是否已经存在,若存在则更新元数据id,否则新增
    			try {
    				
					meta = new FileMetadata();
					meta.setName(file.getOriginalFilename());
					meta.setMimetype(file.getContentType());
					meta.setFilesize(file.getSize());
					server.add(is, meta);
    				resultList.add(meta);
    				
    			} catch (Exception e) {
    				e.printStackTrace();
    				throw new Exception(e);
    			} finally {
    				if (is != null)
    					is.close();
    			}
    		}
    		
    		HashMap<String,Object> d=new HashMap<String, Object>();
    		d.put("data",resultList);
    		String jsonStr=JSON.toJSONString(d,SerializerFeature.DisableCircularReferenceDetect);
    		System.out.println("jsonStr:"+jsonStr);
//    		if(StringUtils.isNotEmpty(callback)){
//    			callback=callback.replaceAll("\\(.*\\)","("+jsonStr+")");
//    		}
    		
    		//Integer.parseInt("sss");
    		
    		//上传成功
    		return new ResponseFactory().createResponseBodyHtml("<script type=\"text/javascript\">" +
    				"var scope=parent.angular.element(\"#"+controlId+"\").scope();" +
    				"scope.$view."+controlId+".$scope.successCallback("+jsonStr+");</script>");
    		
    	}catch(Exception e1){
    		e1.printStackTrace();
    		
    		HashMap<String,Object> reData=new HashMap<String, Object>();
    		reData.put("errorMsg",e1.getMessage());
    		
    		HashMap<String,Object> d=new HashMap<String, Object>();
    		d.put("data",reData);
    		
    		String jsonStr=JSON.toJSONString(d,SerializerFeature.DisableCircularReferenceDetect);
    		//上传失败
    		return new ResponseFactory().createResponseBodyHtml("<script type=\"text/javascript\">" +
    				"var scope=parent.angular.element(\"#"+controlId+"\").scope();" +
    				"scope.$view."+controlId+".$scope.errorCallback("+jsonStr+");</script>");
    	}
    	
    }
}
