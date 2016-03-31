package com.chinacreator.c2.fs.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

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
 * 分片断点续传控制器
 * @author hushowly
 */
@Controller
@RequestMapping(Constants.CHUNK_FILE_PREFIX)
public class ChunkUploadController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ChunkUploadController.class);
	
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
	
	
	private void bubble_sort(File[] unsorted)
    {
        for (int i = 0; i < unsorted.length; i++)
        {
            for (int j = i+1; j < unsorted.length; j++)
            {
            	String iName=unsorted[i].getName();
            	String jName=unsorted[j].getName();
            	
            	Integer iChunk=Integer.parseInt(iName.substring(iName.lastIndexOf("_")+1));
            	Integer jChunk=Integer.parseInt(jName.substring(jName.lastIndexOf("_")+1));
            	
                if (iChunk > jChunk)
                {
                    File temp = unsorted[i];
                    unsorted[i] = unsorted[j];
                    unsorted[j] = temp;
                }
            }
        }
    }
	
	
    /**
     * 合并多个文件到目标文件
     * @param srcfiles
     * @param targetFile
     * @throws Exception
     */
    private long mergeDirFiles(File srcDir,File targetFile)
    {   
    	FileOutputStream writer=null;
    	
    	long fileSize=0;
    	
    	try{
    		
    		byte buffer[]=new byte[1024];  
            int readcount;
            if(!targetFile.getParentFile().exists())  throw new Exception("你合并的 目标文件路径不存在...");  
            writer=new FileOutputStream(targetFile);
            
            File[] srcFiles=srcDir.listFiles();
            
            //按分片排序
            this.bubble_sort(srcFiles);
            
            for(File f:srcFiles)  
            {  
                FileInputStream reader=new FileInputStream(f);  
                while((readcount=reader.read(buffer))!=-1)  
                {  
                    writer.write(buffer);
                    fileSize+=readcount;
                }  
                reader.close();
            }  

    	}catch(Exception e){
    		e.printStackTrace();
    		throw new RuntimeException("合并分片文件出错",e);
    	}finally{
    		if(null!=writer){
    			try{
                    writer.flush();
                    writer.close();
    			}catch(Exception e){e.printStackTrace();};  
    		}
    	}
    	
    	return fileSize;
        
    }
    
    
    /**
     * 续传获取已上传的分片
     * @param request
     * @param guid
     * @param fileName
     * @return
     * @throws IOException
     */
    @RequestMapping(value="{process}/getChunksByGuidAndName")
    public Object getUploadedChunks(HttpServletRequest request,String guid,String fileName) throws IOException {
		
		HashMap<String,Object> resultObj=new HashMap<String, Object>();
		
    	try{
    		
    		Assert.hasText(guid,"guid不能为空!");
    		Assert.hasText(fileName,"fileName不能为空!");

    		File webTempDir=WebUtils.getTempDir(request.getSession().getServletContext());
    		
    		LOGGER.debug("webTempDir:"+webTempDir);
    		
    		//分片保存到临时文件
			String preName=fileName.substring(0,fileName.lastIndexOf("."));
			File chunkTempDir=new File(webTempDir,"chunkTempDir"+File.separator+guid+File.separator+preName);
			List<String> chunkList=new ArrayList<String>();
			if(chunkTempDir.exists()&&chunkTempDir.list().length>0){
				File[] files=chunkTempDir.listFiles();
				for (File file : files) {
					chunkList.add(file.getName().substring(file.getName().lastIndexOf("_")+1));
				}
			}
			resultObj.put("chunks",chunkList);
    	}catch(Exception e){
    		e.printStackTrace();
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
    	}
    	
    	String jsonStr=JSON.toJSONString(resultObj,SerializerFeature.DisableCircularReferenceDetect);
    	return new ResponseFactory().createResponseBodyJSONObject(jsonStr);
    }
	
	
    /**
     * 合并已上传的分片
     * @param request
     * @param guid
     * @param fileName
     * @return
     * @throws IOException
     */
	@RequestMapping(value="{process}/mergeFiles")
    public Object mergeFiles(HttpServletRequest request,@PathVariable String process,String guid,String fileName) throws IOException {
		
		HashMap<String,Object> resultObj=new HashMap<String, Object>();
		
    	try{
    		
    		Assert.hasText(guid,"guid参数不能为空！");
    		Assert.hasText(fileName,"fileName参数不能为空！");
    		
    		UploadProcess uploadProcess=(UploadProcess)this.getProcessInstance(process);
    		
    		List<FileInput> fileInputList=new ArrayList<FileInput>();
    		File webTempDir=WebUtils.getTempDir(request.getSession().getServletContext());
    		
    		File targetFile=new File(webTempDir,"chunkTempDir"+File.separator+guid+File.separator+fileName);
			if(!targetFile.getParentFile().exists()){
				targetFile.getParentFile().mkdirs();
			}
			String preName=fileName.substring(0,fileName.lastIndexOf("."));
			File chunkTempDir=new File(webTempDir,"chunkTempDir"+File.separator+guid+File.separator+preName);
			
			long fileSize=this.mergeDirFiles(chunkTempDir,targetFile);
			
			FileMetadata meta = new FileMetadata();
			meta.setName(targetFile.getName());
			meta.setFilesize(fileSize);
			
    		FileInput fileInput=new FileInput();
    		fileInput.setFileMetadata(meta);
    		FileInputStream targetFileIs=new FileInputStream(targetFile);
			fileInput.setInputStream(targetFileIs);
			fileInputList.add(fileInput);
			
			@SuppressWarnings("unchecked")
			Result uploadResult=uploadProcess.processUpload(fileInputList,request.getParameterMap());
			
			resultObj.put("data",uploadResult);
			
			//关闭输入流
			try{
				targetFileIs.close();
				targetFileIs.close();
				
				//最后删除分片临时文件
				for (File file : chunkTempDir.listFiles()) {
					file.delete();
				}
				chunkTempDir.delete();
				targetFile.delete();
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
    	}catch(Exception e){
    		e.printStackTrace();
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
    	}
    	
    	String jsonStr=JSON.toJSONString(resultObj,SerializerFeature.DisableCircularReferenceDetect);
    	return new ResponseFactory().createResponseBodyJSONObject(jsonStr);
    }
	
    
    /**
     * 文件批量上传
     * @param request    请求对象
     * @param process    处理器
     * @param file       上传文件或分片
     * @param guid       续传实例
     * @return Object   返回回调js或json数据
     * @throws IOException
     */
    @RequestMapping(value="{process}/upload",method = RequestMethod.POST)
    public Object uploadFile(MultipartHttpServletRequest request,@PathVariable String process,MultipartFile file,String guid)
            throws IOException {
    	
    	HashMap<String,Object> resultObj=new HashMap<String, Object>();
    	String chunkStr=request.getParameter("chunk");
    	String chunksStr=request.getParameter("chunks");
    	
    	LOGGER.debug("===uuid:"+guid+" 部分片："+chunksStr+" 当前分片:"+chunkStr+" fileName"+file.getName()+" fileSize:"+file.getSize());
    	
    	try{
    		
    		UploadProcess uploadProcess=(UploadProcess)this.getProcessInstance(process);
    		
    		File webTempDir=WebUtils.getTempDir(request.getSession().getServletContext());
    		
    		LOGGER.debug("webTempDir:"+webTempDir);
    		
    		List<FileInput> fileInputList=new ArrayList<FileInput>();
			
    		//处理分片
    		if(StringUtils.isNotEmpty(chunksStr)&&StringUtils.isNotEmpty(chunkStr)){
    			
    			Integer chunks=Integer.parseInt(chunksStr);
    			Integer chunk=Integer.parseInt(chunkStr);
    			
        		String fileName=file.getOriginalFilename();
				String preName=fileName.substring(0,fileName.lastIndexOf("."));
        		
    			//分片保存到临时文件
    			File chunkTempDir=new File(webTempDir,"chunkTempDir"+File.separator+guid+File.separator+preName);
    			if(!chunkTempDir.exists()){
    				chunkTempDir.mkdirs();
    			}
    			 
        		File chunkTempFile=new File(chunkTempDir,fileName+"_"+chunkStr);
        		
        		file.transferTo(chunkTempFile);
        		
        		Map<String,Object> result=new HashMap<String, Object>();
        		result.put("fileName",fileName);
        		result.put("chunk",Integer.parseInt(chunkStr));
        		result.put("chunks",Integer.parseInt(chunksStr));
        		result.put("chunkName",chunkTempFile.getName());
        		result.put("chunkSize",chunkTempFile.length());
        		result.put("status","ok");
        		resultObj.put("data",result);
        		
    		}else{
    			
    			FileMetadata meta = new FileMetadata();
    			meta.setFilesize(file.getSize());
    			meta.setName(file.getOriginalFilename());
    			meta.setMimetype(file.getContentType());
    			
    			//非分片直接保存
        		FileInput fileInput=new FileInput();
        		fileInput.setFileMetadata(meta);
    			fileInput.setInputStream(file.getInputStream());
    			fileInputList.add(fileInput);
    			
    			Result uploadResult=uploadProcess.processUpload(fileInputList,request.getParameterMap());
    			
    			resultObj.put("data",uploadResult);
    		}
    		
    	}catch(FileNotExsitException fe){
    		fe.printStackTrace();
    		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
    	}catch(Exception e){
    		e.printStackTrace();
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
    	}
    	
    	String jsonStr=JSON.toJSONString(resultObj,SerializerFeature.DisableCircularReferenceDetect);
    	
    	return new ResponseFactory().createResponseBodyJSONObject(jsonStr);
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
    		fe.printStackTrace();
    		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
    	}catch(Exception e){
    		e.printStackTrace();
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

    		String fpath = Util.removeUrlPrefix(request,Constants.CHUNK_FILE_PREFIX);
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
    		fe.printStackTrace();
    		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
    	}catch(Exception e){
    		e.printStackTrace();
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
    		
    		String fpath = Util.removeUrlPrefix(request,Constants.CHUNK_FILE_PREFIX);
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
