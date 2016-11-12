package com.chinacreator.c2.fs.inner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.chinacreator.c2.fs.DownResult;
import com.chinacreator.c2.fs.FileInput;
import com.chinacreator.c2.fs.FileMetadata;
import com.chinacreator.c2.fs.FileServer;
import com.chinacreator.c2.fs.UploadProcess;
import com.chinacreator.c2.fs.exception.FileNotExsitException;
import com.chinacreator.c2.fs.exception.InvalidFilePathException;
import com.chinacreator.c2.fs.util.Constants;
import com.chinacreator.c2.fs.util.Constants.HttpType;
import com.chinacreator.c2.fs.web.FileUploadResult;
import com.chinacreator.c2.fs.web.MultiFileUploadResult;
import com.chinacreator.c2.fs.web.Result;
import com.chinacreator.c2.ioc.ApplicationContextManager;

/**
 * 处理文件上传到目录+数据厍
 * @author hushowly
 */
public class UploadFileProcess extends UploadProcess {
    
    private FileServer dirServer;

    public UploadFileProcess(String processName) {
    	super(processName);
	}
    
    private FileServer getDirFileServer() {
        if (dirServer == null) {
        	dirServer = ApplicationContextManager.getContext().getBean(
                    "fileServer", FileServer.class);
        }
        return dirServer;
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
					FileUploadResult fr=new FileUploadResult(HttpType.SUCCESS.ordinal(),"成功",fileMetadata.getName(),fileMetadata.getId(),Constants.IFRAME_FILE_PREFIX+this.getProcessName()+"/"+fileMetadata.getId()+"/"+fileMetadata.getName());
					fr.setFilesize(fileMetadata.getFilesize());
					fr.setMimetype(fileMetadata.getMimetype());
					mfr.addFileUploadResult(fr);
				}
				uploadResult=mfr;
			}else{
				FileInput fileInput=fileList.get(0);
				InputStream is=fileInput.getInputStream();
				FileMetadata fileMetadata=fileInput.getFileMetadata();
				fileMetadata=server.add(is,fileMetadata);
				FileUploadResult fr=new FileUploadResult(HttpType.SUCCESS.ordinal(),"成功",fileMetadata.getName(),fileMetadata.getId(),Constants.IFRAME_FILE_PREFIX+this.getProcessName()+"/"+fileMetadata.getId()+"/"+fileMetadata.getName());
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
			
			if(StringUtils.isEmpty(fpath)){
				 throw new InvalidFilePathException("文件id不能为空!");
			}
			
			String id=fpath.split("/")[0];
					
			DownResult downResult=new DownResult();

			FileServer server = this.getDirFileServer();
			
			InputStream is=server.get(id);
			FileMetadata fm=server.getMetaData(id);
			if(null==is||null==fm){
				throw new FileNotFoundException(id);
			}
			
			downResult.setInputStream(is);
			downResult.setFileMetadata(fm);
			
			return downResult;
			
		}catch(FileNotFoundException e){
    		throw new FileNotExsitException(fpath);
		}
	}

	
	@Override
	public boolean exist(String fpath, Map<String, String[]> params) throws Exception{
		
		if(StringUtils.isEmpty(fpath)){
			 throw new InvalidFilePathException("文件id不能为空!");
		}
		FileServer server = this.getDirFileServer();
		String fid=fpath.split("/")[0];
		return server.exsits(fid);
	}
	
	
	@Override
	public boolean processDelete(String fpath, Map<String, String[]> params) throws Exception{
		
		
		try{
			
			if(StringUtils.isEmpty(fpath)){
				 throw new InvalidFilePathException("文件id不能为空!");
			}
			
			FileServer server = this.getDirFileServer();
			String fid=fpath.split("/")[0];
			return server.delete(fid,true);
		}catch(FileNotFoundException e){
    		throw new FileNotExsitException(fpath);
		}
	}
	
}
