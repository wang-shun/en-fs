package com.chinacreator.c2.fs.view;

import com.chinacreator.c2.dao.Dao;
import com.chinacreator.c2.dao.DaoFactory;
import com.chinacreator.c2.fs.FileMetadata;
import com.chinacreator.c2.fs.FileServer;
import com.chinacreator.c2.ioc.ApplicationContextManager;

public class FileServices {
	
	/**
	 * 删除文件
	 * @param fId
	 */
	public static void deleteFile(String fId) throws Exception{
		
		Dao<FileMetadata> dao=DaoFactory.create(FileMetadata.class);
		FileMetadata meta = new FileMetadata();
		meta.setId(fId);
		int count=dao.count(meta);
		if(count<=0){
			throw new RuntimeException("没有找到编号为["+fId+"]的文件");
		}
		
		FileServer fileServer =ApplicationContextManager.getContext().getBean("fileServer", FileServer.class);
		if(count>1){
			dao.delete(meta);
		}else{
			if(fileServer.exsits(fId)){
				fileServer.delete(fId,true);
			}
			dao.delete(meta);
		}
	}
	
	
	/**
	 * 文件重命名
	 * @param fId
	 * @param newName
	 */
	public static void reNameFile(String fId,String newName){
		Dao<FileMetadata> dao=DaoFactory.create(FileMetadata.class);
		FileMetadata meta = new FileMetadata();
		meta.setFileid(fId);
		meta=dao.selectOne(meta);
		if(null!=meta){
			meta.setName(newName);
			dao.update(meta);
		}else{
			throw new RuntimeException("没有找到编号为["+fId+"]的文件");
		}
	}
}