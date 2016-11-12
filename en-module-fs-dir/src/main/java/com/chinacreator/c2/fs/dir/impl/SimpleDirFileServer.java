package com.chinacreator.c2.fs.dir.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinacreator.c2.fs.FileMetadata;
import com.chinacreator.c2.fs.HTTPFileServer;
import com.chinacreator.c2.fs.exception.FileNotExsitException;
import com.chinacreator.c2.fs.util.ContentTypeUtil;
import com.chinacreator.c2.fs.util.PathFormat;

/**
 * 本地简单目录文件服务器实现
 * @author hushowly
 */
public class SimpleDirFileServer implements HTTPFileServer{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDirFileServer.class);
	private File root;
	private boolean valid = false;
	private String pathFormat;
	private Long maxSize;
	private String allowFiles;
	
	
	public void init(String rootPath){
		this.root = new File(rootPath);
		if (this.root.exists() && this.root.isDirectory()) {
			this.valid = true;
		} else {
			boolean succes = this.root.mkdirs();
			if (!succes) {
				LOGGER.error("本地简单目录文件服务器初始化失败，根目录" + root.getAbsolutePath()
						+ "不存在且无法被创建，" + "可能是存在同名文件，或没有权限创建目录");
			}
		}
		try {
			this.valid = true;
			LOGGER.info("简单目录文件服务器初始化成功，储存根目录：{}", root.getAbsolutePath());
		} catch (Exception e) {
			LOGGER.error("初始化目录文件服务器时发生错误:{}，简单目录文件服务器无效!!!", e.getMessage());
		}
	}
	
	/**
	 * 创建简单目录文件服务器
	 * @param rootPath
	 */
	public SimpleDirFileServer(String rootPath) {
		this.init(rootPath);
	}
	
	
	/**
	 * 创建简单目录文件服务器
	 * @param rootPath
	 */
	public SimpleDirFileServer(String rootPath,String pathFormat) {
		this.init(rootPath);
		this.pathFormat=pathFormat;
	}
	
	
	/**
	 * 创建简单目录文件服务器
	 * @param rootPath
	 */
	public SimpleDirFileServer(String rootPath,String pathFormat,Long maxSize){
		this.init(rootPath);
		this.pathFormat=pathFormat;
		this.maxSize=maxSize;
	}
	
	/**
	 * 创建简单目录文件服务器
	 * @param rootPath
	 */
	public SimpleDirFileServer(String rootPath,String pathFormat,Long maxSize,String allowFiles){
		this.init(rootPath);
		this.pathFormat=pathFormat;
		this.maxSize=maxSize;
		this.allowFiles=allowFiles;
	}
	

	public static void main(String[] args) throws IOException {
		//String input="e:/comunity_dir1_files/{yyyy}{mm}{dd}/{time}{rand:6}.pdf";
		//Pattern pattern = Pattern.compile( "\\{([^\\}]+)\\}", Pattern.CASE_INSENSITIVE  );
		//Matcher matcher = pattern.matcher(input);
		
		String str="a/ab";
		System.out.println(str.substring(-1));
		
	}
	

	@Override
	public FileMetadata add(InputStream content,FileMetadata metadata)
			throws IOException {
		FileOutputStream outputStream =null;
		File file=null;

		try {
			
			String fileName=metadata.getName();
			String filePath=metadata.getName();
			//创建父目录
			if(StringUtils.isNotEmpty(this.pathFormat)){
				
				if(filePath.lastIndexOf(".")!=-1){
					String suffix = fileName.substring(fileName.lastIndexOf("."));
					String preName=fileName.substring(0,fileName.lastIndexOf("."));
					filePath=PathFormat.parse(this.pathFormat,preName);
					filePath+=suffix;
				}else{
					filePath=PathFormat.parse(this.pathFormat,fileName);
				}
				
				file=new File(this.root,filePath);
				
				File parentFile = file.getParentFile();
		        if (parentFile != null&&!parentFile.exists()) {
		            if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
		                throw new IOException("Destination '" + parentFile + "' directory cannot be created");
		            }
		        }
			}else{
				file=new File(this.root,filePath);
			}
			
			//pathFomart后存在重名，自动重命名
			int i=1;
			while(file.exists()){
				String reName=filePath;
				if(filePath.lastIndexOf(".")!=-1){
					String pre=filePath.substring(0,filePath.lastIndexOf("."));
					String sub= filePath.substring(filePath.lastIndexOf("."),filePath.length());
					reName=pre+"["+i+"]"+sub;
				}else{
					reName+="["+i+"]";
				}
				i++;
				file=new File(this.root,reName);
				if(!file.exists()){
					filePath=reName;
					break;
				}
			}
			
			outputStream=new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = content.read(buffer)) != -1) {
				outputStream.write(buffer, 0, length);
			}
			
			metadata.setPath(filePath);
			metadata.setName(file.getName());
			metadata.setFilesize(file.length());
			MediaType type = ContentTypeUtil.getContentType(
					new BufferedInputStream(content),metadata.getName());
			metadata.setMimetype(type.toString());
		} catch (Exception exception) {
			throw new RuntimeException("写文件时出现导常",exception);
		} finally {
			if(null!=outputStream)outputStream.close();
		}
		return metadata;
	}
	
	
	@Override
	public boolean delete(String path, boolean force) {
		File file=new File(root,path);
		return file.delete();
	}
	
	@Override
	public boolean exsits(String path) {
		File dir=new File(root,path);
		return dir.exists();
	}
	
	
	@Override
	public InputStream get(String path) throws IOException {
		File file=new File(root,path);
		return new FileInputStream(file);
	}
	
	@Override
	public FileMetadata getMetaData(String path) throws Exception{
		FileInputStream fi=null;
		try{
			File file=new File(root,path);
			if(!file.exists()) throw new FileNotExsitException(path);
			FileMetadata fm=new FileMetadata();
			fm.setName(file.getName());
			fm.setFilesize(file.length());
			fi=new FileInputStream(file);
			MediaType type = ContentTypeUtil.getContentType(new BufferedInputStream(fi),file.getName());
			fm.setMimetype(type.toString());
			return fm;
		}catch(FileNotExsitException e){
			e.printStackTrace();
			throw new FileNotExsitException(path);
		}catch(IOException iex){
			iex.printStackTrace();
			throw new IOException(iex);
		}finally{
			if(null!=fi){
				fi.close();
			}
		}
	}
	
	
	@Override
	public String getUrl(String path) throws Exception {
		return path+"/"+getMetaData(path).getName();
	}
	
	
	@Override
	public String parsePath(String urlPath){
		return urlPath.substring(0,urlPath.indexOf("/"));
	}
	
	
	@Override
	public boolean isValid() throws Exception {
		return this.valid;
	}
	
	
	@Override
	public boolean update(String path,InputStream content) throws Exception {
		File file=new File(root,path);
		if(!file.exists()) throw new FileNotExsitException(path);
		FileOutputStream outputStream=new FileOutputStream(file);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = content.read(buffer)) != -1) {
			outputStream.write(buffer, 0, length);
		}
		return true;
	}
	
}
