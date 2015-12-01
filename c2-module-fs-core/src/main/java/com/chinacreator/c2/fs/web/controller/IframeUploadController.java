package com.chinacreator.c2.fs.web.controller;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.chinacreator.c2.fs.DownResult;
import com.chinacreator.c2.fs.FileInput;
import com.chinacreator.c2.fs.FileMetadata;
import com.chinacreator.c2.fs.UploadProcess;
import com.chinacreator.c2.fs.exception.FileNotExsitException;
import com.chinacreator.c2.fs.exception.InvalidFilePathException;
import com.chinacreator.c2.fs.util.Constants;
import com.chinacreator.c2.fs.util.Constants.HttpType;
import com.chinacreator.c2.fs.web.Result;
import com.chinacreator.c2.fs.web.Util;
import com.chinacreator.c2.ioc.ApplicationContextManager;
import com.chinacreator.c2.web.controller.ResponseFactory;

/**
 * 处理目录文件下载请求的控制器
 * @author hushowly
 */
@Controller
@RequestMapping(Constants.IFRAME_FILE_PREFIX)
public class IframeUploadController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IframeUploadController.class);
	
	/**
	 * 反射文件处理器实例
	 * @param className
	 * @return
	 * @throws Exception
	 */
	private UploadProcess getProcessInstance(String processName){
		
		UploadProcess processInstance=null;
	
		Map<String,UploadProcess> beansMap=ApplicationContextManager.getContext().getBeansOfType(UploadProcess.class);
		for (Map.Entry<String,UploadProcess> entry : beansMap.entrySet()) {
			if(processName.equals(entry.getValue().getProcessName())){
				processInstance=entry.getValue();
			}
		}
		
		if(null==processInstance) throw new RuntimeException("找不到文件上传下载处理器["+ processName + "]的实例");

		return processInstance;
	}
	
    
    /**
     * 文件批量上传
     * @param request   请求对象
     * @param controlId 控件id
     * @param callback  上传成功回调函数
     * @return Object   返回回调js
     * @throws IOException
     */
    @RequestMapping(value="{process}/upload",method = RequestMethod.POST)
    public Object uploadFile(MultipartHttpServletRequest request,@PathVariable String process,String controlId)
            throws IOException {
    	
    	
    	HashMap<String,Object> resultObj=new HashMap<String, Object>();
    	
    	try{

    		UploadProcess uploadProcess=(UploadProcess)this.getProcessInstance(process);
    		
        	MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        	//文件列表
        	Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        	LinkedList<FileMetadata> resultList=new LinkedList<FileMetadata>();
        	
        	//验证文件大小 
        	List<FileInput> fileInputList=new ArrayList<FileInput>();
        	List<FileMetadata> validateList=new ArrayList<FileMetadata>();
        	String maxSizeStr=request.getParameter("maxSize");

        	for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
        		CommonsMultipartFile file = (CommonsMultipartFile) entity.getValue();
        		
        		FileMetadata meta = new FileMetadata();
    			meta.setFilesize(file.getSize());
    			meta.setName(file.getOriginalFilename());
    			meta.setMimetype(file.getContentType());
    			
            	if(StringUtils.isNotEmpty(maxSizeStr)&&StringUtils.isNumeric(maxSizeStr)){
            		int maxSize=Integer.parseInt(maxSizeStr);
            		if(file.getSize()>maxSize){
            			validateList.add(meta);
            		}
            	}
        
        		FileInput fileInput=new FileInput();
        		fileInput.setFileMetadata(meta);
    			fileInput.setInputStream(file.getInputStream());
    			fileInputList.add(fileInput);
        	}
        	
        	
        	if(validateList.size()>0){
        		HashMap<String,Object> reData=new HashMap<String, Object>();
        		reData.put("errorMsg","单个文件大小超过限制！");
        		reData.put("files",validateList);
        		resultObj.put("data",reData);
        	}else{
            	Result uploadResult=uploadProcess.processUpload(fileInputList,request.getParameterMap());
            	resultObj.put("data",uploadResult);
        	}

    	}catch(Exception e1){
    		e1.printStackTrace();
    	
    		HashMap<String,Object> reData=new HashMap<String, Object>();
    		reData.put("errorMsg",e1.getMessage());
    		resultObj.put("data",reData);
    	}
    	
    	//处理平台web控件上传
		if(StringUtils.isNotEmpty(controlId)){
    		String jsonStr=JSON.toJSONString(resultObj,SerializerFeature.DisableCircularReferenceDetect);
    		
    		//上传成功
    		return new ResponseFactory().createResponseBodyHtml("<script type=\"text/javascript\">" +
    				"var scope=parent.angular.element(\"#"+controlId+"\").scope();" +
    				"scope.$view."+controlId+".$scope.successCallback("+jsonStr+");</script>");
		}else{
			return new ResponseFactory().createResponseBodyJSONObject(resultObj);
		}
    }
    

    /**
     * 下载附件，返回统一二进制流，不会被浏览器自动识别
     * @param id
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value ="{process}/download/**")
    public Object downFile(@PathVariable String process,HttpServletRequest request,HttpServletResponse response)
            throws IOException {
    	
    	InputStream is=null;
    	
    	try{
    		
    		String fpath = Util.removeUrlPrefix(request,Constants.IFRAME_FILE_PREFIX);
    		fpath=java.net.URLDecoder.decode(fpath, "UTF-8");
    		fpath=fpath.replaceFirst(process+"/download/","");
    		
	        if (StringUtils.isEmpty(fpath)) {
	            throw new InvalidFilePathException("文件id不能为空!");
	        }

	        
	        UploadProcess uploadProcess=this.getProcessInstance(process);
	        
	        DownResult downResult=uploadProcess.processDown(fpath,request.getParameterMap());
	        if (null==downResult||downResult.getInputStream() == null) {
	            throw new FileNotExsitException(fpath);
	        }
	        
	        //处理断点下载
	        LOGGER.debug( "request.getHeader(\"Range\")=" + request.getHeader("Range" ));
	        
	        long pos = 0; 
    		long fileLength=downResult.getFileMetadata().getFilesize();
    		if (null!=request.getHeader( "Range")) { // 客户端请求的下载的文件块的开始字节
    			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                try {
                    pos = Long.parseLong(request.getHeader("Range").replaceAll("bytes=", "").replaceAll("-", ""));    
                } catch (NumberFormatException e) {  
                    pos = 0;    
                }
    		}
	        
	        response.addHeader( "Content-Disposition" ,  "attachment; filename="  + java.net.URLEncoder.encode(downResult.getFileMetadata().getName(), "UTF-8")); 
	        response.setContentType("application/octet-stream");
	        response.setHeader("Content-Length",(fileLength-pos)+"");
	        response.setHeader("Accept-Ranges", "bytes"); 
	        String contentRange = new StringBuffer("bytes ").append(pos+"").append("-").append((fileLength - 1)+"").append("/").append(fileLength+"").toString();
	        response.setHeader("Content-Range", contentRange);
	        LOGGER.debug("Content-Range:"+contentRange);
	
	        is=downResult.getInputStream();
	        is.skip(pos);
	        
	        OutputStream stream = response.getOutputStream();
	
	        long writeSize=0;
			byte[] buffer = new byte[1024*8];
			int length=0,buffred=0;
			int maxBufferSize=1024*1024;//最多缓存1M数据
			while ((length = is.read(buffer)) != -1) {
				stream.write(buffer, 0, length);
				writeSize+=length;
				buffred += length;
		        if (buffred > maxBufferSize) { 
		        	buffred = 0;
		        	stream.flush();
		        }
			}
	
	        stream.flush();
	        
	        LOGGER.debug("writeSize:"+writeSize+"  pos+writeSize："+(writeSize+pos)+" 实际总大小:"+downResult.getFileMetadata().getFilesize());
	        
	        return null;
	        
    	}catch(FileNotExsitException fe){
    		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
    	}catch(Exception e){
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
    	}finally{
    		if(null!=is){
    			is.close();
    		}
    	}
    }
    
    
    /**
     * 下载附件，会被浏览器自动识别打开
     * @param id
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value ="{process}/exist/**")
    public Object exist(@PathVariable String process,HttpServletRequest request,HttpServletResponse response)
            throws Exception {
    	
    	Result result=null;
    	
    	try{
    		
    		String fpath = Util.removeUrlPrefix(request,Constants.IFRAME_FILE_PREFIX);
    		fpath=java.net.URLDecoder.decode(fpath, "UTF-8");
    		fpath=fpath.replaceFirst(process+"/exist/","");
    		
	        if (StringUtils.isEmpty(fpath)) {
	            throw new InvalidFilePathException("文件id不能为空!");
	        }
	        
	        UploadProcess uploadProcess=this.getProcessInstance(process);
	        
	        boolean exist=uploadProcess.exist(fpath,request.getParameterMap());
	        
	        if (exist) {
	        	result=new Result(HttpType.SUCCESS.ordinal(),"存在["+fpath+"]附件");
	        }else{
	        	result=new Result(HttpType.ERROR.ordinal(),"不存在["+fpath+"]附件");
	        }
	        
    	}catch(Exception e){
    		e.printStackTrace();
        	result=new Result(HttpType.ERROR.ordinal(),"查询附件异常:"+e.getMessage());
    	}
    	
    	return new ResponseFactory().createResponseBodyJSONObject(result);
    }
    
    
    /**
     * 下载附件，会被浏览器自动识别打开
     * @param id
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value ="{process}/**",method = RequestMethod.GET)
    public Object openFile(@PathVariable String process,HttpServletRequest request,HttpServletResponse response)
            throws IOException {

    	InputStream is=null;
    	OutputStream stream=null;
    	
    	try{

    		String fpath = Util.removeUrlPrefix(request,Constants.IFRAME_FILE_PREFIX);
    		fpath=java.net.URLDecoder.decode(fpath, "UTF-8");
    		fpath=fpath.replaceFirst(process+"/","");
    		
	        if (StringUtils.isEmpty(fpath)) {
	            throw new InvalidFilePathException("文件id不能为空!");
	        }
	        
	        
	        UploadProcess uploadProcess=this.getProcessInstance(process);
	        
	        DownResult downResult=uploadProcess.processDown(fpath,request.getParameterMap());
	        if (null==downResult||downResult.getInputStream() == null) {
	            throw new FileNotExsitException(fpath);
	        }
	        
	        LOGGER.debug( "request.getHeader(\"Range\")=" + request.getHeader("Range" ));
	        
	        //处理断点下载
	        long pos = 0; 
    		long fileLength=downResult.getFileMetadata().getFilesize();
    		if (null!=request.getHeader( "Range")) { // 客户端请求的下载的文件块的开始字节
    			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                try {
                    pos = Long.parseLong(request.getHeader("Range").replaceAll("bytes=", "").replaceAll("-", ""));    
                } catch (NumberFormatException e) {  
                    pos = 0;    
                }
    		}
    		
	        //response.addHeader( "Content-Disposition" ,  "attachment; filename="  + java.net.URLEncoder.encode(downResult.getFileMetadata().getName(), "UTF-8"));
	        response.setContentType(downResult.getFileMetadata().getMimetype());
	        response.setHeader("Content-Length",(fileLength-pos)+"");
	        response.setHeader("Accept-Ranges", "bytes"); 
	        
	        String contentRange = new StringBuffer("bytes ").append(pos+"").append("-").append((fileLength - 1)+"").append("/").append(fileLength+"").toString();
	        response.setHeader("Content-Range", contentRange);
	        LOGGER.debug("Content-Range:"+contentRange);
	        
	        is=downResult.getInputStream();
	        is.skip(pos);
	        
	        stream = response.getOutputStream();
	
	        long writeSize=0;
			byte[] buffer = new byte[1024*8];
			int length=0,buffred=0;
			int maxBufferSize=1024*1024;//最多缓存1M数据
			while ((length = is.read(buffer)) != -1) {
				stream.write(buffer, 0, length);
				writeSize+=length;
				buffred += length;
		        if (buffred > maxBufferSize) { 
		        	buffred = 0;
		        	stream.flush();
		        }
			}
	        
	       
	        LOGGER.debug("writeSize:"+writeSize+"  pos+writeSize："+(writeSize+pos)+" 实际总大小:"+downResult.getFileMetadata().getFilesize());
	        return null;
	        
    	}catch(FileNotExsitException fe){
    		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
    	}catch(Exception e){
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
    	}finally{
    		
    		if(null!=stream){
    			stream.flush();
    		}
    		 
    		if(null!=is){
    			is.close();
    		}
    		
    		if(null!=stream){
    			stream.close();
    		}
    	}
    }
    
    
    /**
     * 删除上传的附件
     * @param id
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value ="{process}/**",method = RequestMethod.DELETE)
    public Object deleteFile(@PathVariable String process,HttpServletRequest request,HttpServletResponse response)
            throws IOException {
    	
    	Result result=null;
    	
    	try{
    		
    		String fpath = Util.removeUrlPrefix(request,Constants.IFRAME_FILE_PREFIX);
    		fpath=java.net.URLDecoder.decode(fpath, "UTF-8");
    		fpath=fpath.replaceFirst(process+"/","");
    		
	        if (StringUtils.isEmpty(fpath)) {
	            throw new InvalidFilePathException("文件id不能为空!");
	        }
	        
	        UploadProcess uploadProcess=this.getProcessInstance(process);
	        
	        boolean isDel=uploadProcess.processDelete(fpath,request.getParameterMap());
	        
	        if (isDel==false) {
	        	result=new Result(HttpType.ERROR.ordinal(),"删除失败");

	        }else{
	        	result=new Result(HttpType.SUCCESS.ordinal(),"成功删除");
	        }
	        
    	}catch(Exception e){
    		e.printStackTrace();
        	result=new Result(HttpType.ERROR.ordinal(),"删除失败,"+e.getMessage());
    	}
    	
    	return new ResponseFactory().createResponseBodyJSONObject(result);
    }
    
    public static void main(String[] args) {
    	File file=new File("d:\\comunity_dirTest_files\\20150427\\060557\\error.png");
    	System.out.println(file.delete());
	}

    
}
