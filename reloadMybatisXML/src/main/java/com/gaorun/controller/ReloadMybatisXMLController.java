package com.gaorun.controller;

import com.gaorun.core.XMLMapperLoader;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *@Description
 *@author gaorun
 *@create 2022-03-31 14:14
 */
@RestController
@RequestMapping("/mybatis")
public class ReloadMybatisXMLController {

	Logger log = Logger.getLogger(ReloadMybatisXMLController.class);

	@Autowired
	private SqlSessionFactory sqlSessionFactory;
	//	@Bean
	//	public XMLMapperLoader xMLMapperLoader() {
	//		return new XMLMapperLoader(sqlSessionFactory,"classpath:mapping/*.xml");
	//	}

	/**
	 * 第二种方式 通过URL来刷新
	 * @return
	 */
	@RequestMapping(value = "/refersh/{xmlMappingPath}")
	public String refershMyBatisXml(@PathVariable String xmlMappingPath) {
		if (xmlMappingPath != null) {
			String[] split = xmlMappingPath.split("-");
			String reloadPath = String.join("/", split);
			log.warn("reload path : " + reloadPath);
			return new XMLMapperLoader(sqlSessionFactory, "classpath:" + reloadPath).readMapperXml();
		}
		return "reload path is " + xmlMappingPath;
	}
}
