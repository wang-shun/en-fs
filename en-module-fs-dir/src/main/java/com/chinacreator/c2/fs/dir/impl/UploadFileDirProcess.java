package com.chinacreator.c2.fs.dir.impl;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.chinacreator.c2.fs.DownResult;
import com.chinacreator.c2.fs.FileInput;
import com.chinacreator.c2.fs.FileMetadata;
import com.chinacreator.c2.fs.FileServer;
import com.chinacreator.c2.fs.UploadProcess;
import com.chinacreator.c2.fs.exception.FileNotExsitException;
import com.chinacreator.c2.fs.util.Constants;
import com.chinacreator.c2.fs.util.Constants.HttpType;
import com.chinacreator.c2.fs.web.FileUploadResult;
import com.chinacreator.c2.fs.web.MultiFileUploadResult;
import com.chinacreator.c2.fs.web.Result;
import com.chinacreator.c2.ioc.ApplicationContextManager;


/**
 * 处理文件上传到目录
 * @author hushowly
 */
public class UploadFileDirProcess extends UploadProcess{
	
    private FileServer dirFileServer;
    
    private boolean fileNameEncoding=true;  //默认开启，如果兼容旧代码前台作了编码处理请配置为false
    
    
	public boolean isFileNameEncoding() {
		return fileNameEncoding;
	}

	public void setFileNameEncoding(boolean fileNameEncoding) {
		this.fileNameEncoding = fileNameEncoding;
	}

	public UploadFileDirProcess(String processName) {
		super(processName);
	}

    private FileServer getDirFileServer() {
        if (dirFileServer == null) {
        	dirFileServer = ApplicationContextManager.getContext().getBean(
                    "dirFileServer", FileServer.class);
        }
        return dirFileServer;
    }
    
	@Override
	public Result processUpload(List<FileInput> fileList,
			Map<String, String[]> params) throws Exception{

		Result uploadResult=null;
		
		try{

			if(fileList.size()<=0) throw new Exception("上传文件不能为空");
			
			FileServer server = this.getDirFileServer();
			
			//为了兼容以前，单附件和多附件格式不能统一
			if(fileList.size()>1){
				MultiFileUploadResult mfr=new MultiFileUploadResult(HttpType.SUCCESS.ordinal(),"成功");
				
				for (FileInput fileInput : fileList) {
					InputStream is=fileInput.getInputStream();
					FileMetadata fileMetadata=fileInput.getFileMetadata();
					fileMetadata=server.add(is,fileMetadata);
					//将保存后的附件信息添加到结果集中
					
					//附件保存完后，将文件名特殊字符统一做编码，否则无法下载
					if(fileNameEncoding){
						String[] items=fileMetadata.getPath().split("/");
						if(items.length>0) items[items.length-1]=URLEncoder.encode(items[items.length-1],"UTF-8");
						fileMetadata.setPath(StringUtils.join(items,"/"));
					}
					
					FileUploadResult fr=new FileUploadResult(HttpType.SUCCESS.ordinal(),"成功",fileMetadata.getName(),fileMetadata.getPath(),Constants.IFRAME_FILE_PREFIX+this.getProcessName()+"/"+fileMetadata.getPath());
					fr.setFilesize(fileMetadata.getFilesize());
					fr.setMimetype(fileMetadata.getMimetype());
					mfr.addFileUploadResult(fr);
				}
				uploadResult=mfr;
			}else{
				FileInput fileInput=fileList.get(0);
				InputStream is=fileInput.getInputStream();
				FileMetadata fileMetadata=fileInput.getFileMetadata();
				fileMetadata=server.add(is,fileInput.getFileMetadata());
				
				//附件保存完后，将文件名特殊字符统一做编码，否则无法下载
				if(fileNameEncoding){
					String[] items=fileMetadata.getPath().split("/");
					if(items.length>0) items[items.length-1]=URLEncoder.encode(items[items.length-1],"UTF-8");
					fileMetadata.setPath(StringUtils.join(items,"/"));
				}
				
				FileUploadResult fr=new FileUploadResult(HttpType.SUCCESS.ordinal(),"成功",fileMetadata.getName(),fileMetadata.getPath(),Constants.IFRAME_FILE_PREFIX+this.getProcessName()+"/"+fileMetadata.getPath());
				fr.setFilesize(fileMetadata.getFilesize());
				fr.setMimetype(fileMetadata.getMimetype());
				uploadResult=fr;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("上传文件失败:"+e.getMessage());
		}
		
		return uploadResult;
	}
	
	
	@Override
	public DownResult processDown(String fpath, Map<String, String[]> params) throws Exception{
		
		try{
			
			DownResult downResult=new DownResult();

			FileServer server = this.getDirFileServer();
			
			InputStream is=server.get(fpath);
			FileMetadata fm=server.getMetaData(fpath);
			
			downResult.setInputStream(is);
			downResult.setFileMetadata(fm);
			
			return downResult;
			
		}catch(FileNotFoundException e){
    		throw new FileNotExsitException(fpath);
		}
	}

	
	@Override
	public boolean exist(String fpath, Map<String, String[]> params) throws Exception{
		FileServer server = this.getDirFileServer();
		return server.exsits(fpath);
	}
	
	
	@Override
	public boolean processDelete(String fpath, Map<String, String[]> params) throws Exception{
		try{
			FileServer server = this.getDirFileServer();
			return server.delete(fpath,true);
		}catch(FileNotFoundException e){
    		throw new FileNotExsitException(fpath);
		}
	}
	
}
