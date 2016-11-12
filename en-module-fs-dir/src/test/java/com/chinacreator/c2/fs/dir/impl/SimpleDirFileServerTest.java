package com.chinacreator.c2.fs.dir.impl;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.chinacreator.c2.fs.FileMetadata;
import com.chinacreator.c2.fs.FileServer;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("unit_test")
@ContextConfiguration(locations = "classpath:spring/application.xml")
public class SimpleDirFileServerTest 
extends AbstractJUnit4SpringContextTests {
	
	@Autowired
	private FileServer dirFileServer;

	@Test
	public void testAddFile() throws Exception{
		ByteArrayInputStream bi=new ByteArrayInputStream("测试文本abc2222222222".getBytes());
		FileMetadata fm=new FileMetadata();
		fm.setName("测试文本abc1111111111111.PDF");
		fm=dirFileServer.add(bi,fm);
	}
}
