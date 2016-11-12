package com.chinacreator.c2.fs.dir.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chinacreator.c2.fs.FileMetadata;
import com.chinacreator.c2.fs.dir.DirFile;
import com.chinacreator.c2.fs.dir.DirFileServer;
import com.chinacreator.c2.fs.dir.DirFileVo;
import com.chinacreator.c2.fs.exception.FileNotExsitException;
import com.chinacreator.c2.ioc.ApplicationContextManager;
import com.chinacreator.c2.web.controller.ResponseFactory;

/**
 * 处理目录文件下载请求的控制器
 * @author hushowly
 */
@Controller
@RequestMapping("/dirfile")
public class DirFileController {
    private DirFileServer dirFileServer;

    private DirFileServer getDirFileServer() {
        if (dirFileServer == null) {
        	dirFileServer = ApplicationContextManager.getContext().getBean(
                    "dirFileServer", DirFileServer.class);
        }
        return dirFileServer;
    }
    
    
    /**
     * 检查附件是否存在
     * @param filePath
     * @param response
     * @return
     */
    @RequestMapping(value = "/{filePath}", method = RequestMethod.GET,headers={"type=check"})
    public Object checkExist(@PathVariable String filePath, HttpServletResponse response) {
    	HashMap<String,Object> param=new HashMap<String,Object>();
    	try{
    		DirFileServer server = getDirFileServer();
	        boolean is = server.exsits(filePath);
	        if(is){
	        	param.put("checkStatus",true);
	        }else{
	        	param.put("checkStatus",false);
	        }
    	}catch(Exception e){
        	e.printStackTrace();
        	param.put("checkStatus",false);
        	param.put("checkMsg",e.getMessage());
        }
    	return new ResponseFactory().createResponseBodyJSONObject(param);
    }
    
    
    /**
     * 下载附件，返回统一二进制流，不会被浏览器自动识别
     * @param id
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "download/{filePath}/*", method = RequestMethod.GET)
    public Object downFile(@PathVariable String filePath, HttpServletResponse response)
            throws IOException {
    	
    	try{
    		
	        if (StringUtils.isEmpty(filePath)) {
	            throw new Exception("文件路径不能为空!");
	        }
	        
	        DirFile dirFile=new DirFile(filePath);
	        DirFileServer server = getDirFileServer();
	        InputStream inputStream = server.getFileStream(filePath);
	        if (inputStream == null) {
	            throw new FileNotExsitException(filePath);
	        }
	        
	        response.addHeader( "Content-Disposition","attachment; filename="  + java.net.URLEncoder.encode(dirFile.getFileName(), "UTF-8")); 
	        response.setContentType("application/octet-stream");
	        //response.setContentLength(inputStream.);
	
	        OutputStream stream = response.getOutputStream();
	
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = inputStream.read(buffer)) != -1) {
	            stream.write(buffer, 0, length);
	        }
	
	        stream.flush();
	        stream.close();
	        inputStream.close();

	        return null;
    	}catch(Exception e){
    		e.printStackTrace();
    		return new ResponseFactory().createResponseBodyHtml("<script type=\"text/javascript\">alert('"+e.getMessage()+"');</script>");
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
    		
    		String dirPath=request.getParameter("dirPath");
    				
    		DirFileServer server = this.getDirFileServer();
        	MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        			
        	//文件列表
        	Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        	LinkedList<DirFileVo> resultList=new LinkedList<DirFileVo>();
        	
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
    			String fileName=file.getOriginalFilename();
    			System.out.println("处理上传文件：" + file.getOriginalFilename());
    			
    			if(StringUtils.isEmpty(dirPath)){
    				dirPath="";
    			}
    			//判断重复,自动重命名
    			
    			boolean isExsits=server.exsits(dirPath+"\\"+fileName);
    			int i=1;
    			while(isExsits){
    				if(fileName.lastIndexOf(".")!=-1){
    					String pre=file.getOriginalFilename().substring(0,file.getOriginalFilename().lastIndexOf("."));
    					String sub= file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."),file.getOriginalFilename().length());
    					fileName=pre+"["+i+"]"+sub;
    				}else{
    					fileName+="["+i+"]";
    				}
    				i++;
    				
    				isExsits=server.exsits(dirPath+File.separator+fileName);
    				if(!isExsits){
    					break;
    				}
    			}
    			
    			InputStream is = file.getInputStream();
    			try {
    
    				DirFileVo dirvo = new DirFileVo();
    				dirvo.setFileName(fileName);
    				dirvo.setFilesize(file.getSize());
					DirFile saveDirFile=server.add(is, dirPath+File.separator+fileName);
					dirvo.setMimetype(saveDirFile.getMimetype());
    				resultList.add(dirvo);
    				
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
