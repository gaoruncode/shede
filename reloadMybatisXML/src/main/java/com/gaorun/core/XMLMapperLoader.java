package com.gaorun.core;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author gaorun
 *
 */
public class XMLMapperLoader {

	private Logger logger = Logger.getLogger(XMLMapperLoader.class);

	private SqlSessionFactory sqlSessionFactory;
	private Resource[] mapperLocations;
	private String packageSearchPath = "classpath:mapping/**/*.xml";
	private HashMap<String, Long> fileMapping = new HashMap<String, Long>();

	public XMLMapperLoader(SqlSessionFactory sqlSessionFactory, String packageSearchPath) {
		if (this.sqlSessionFactory == null) {
		}
		this.sqlSessionFactory = sqlSessionFactory;
		if (packageSearchPath != null && packageSearchPath != "") {
			this.packageSearchPath = packageSearchPath;
			//启动自动刷新，调试时容易卡死
			startThreadListener();
		}
	}

	public void startThreadListener() {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		//每5秒执行一次
		service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				readMapperXml();
			}
		}, 10, 10, TimeUnit.SECONDS);
	}

	public String readMapperXml() {
		try {
			org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
			// step.1 扫描文件
			try {
				this.scanMapperXml();
			} catch (IOException e) {
				return "packageSearchPath扫描包路径配置错误";
			}
			//			for (String name : configuration.getMappedStatementNames()) {
			//				logger.warn(name);
			//			}
			// step.2 判断是否有文件发生了变化
			if (this.isChanged()) {
				// step.2.1 清理
				this.removeConfig(configuration);
				// step.2.2 重新加载
				for (Resource configLocation : mapperLocations) {
					try {
						XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(configLocation.getInputStream(), configuration, configLocation.toString(),
						                                                         configuration.getSqlFragments());
						xmlMapperBuilder.parse();
						logger.warn("mapper文件[" + configLocation.getFilename() + "]缓存加载成功");
					} catch (IOException e) {
						logger.warn("mapper文件[" + configLocation.getFilename() + "]不存在或内容格式不对");
						continue;
					}
				}
				System.out.println("---------------------------------------------------------------");
			}
			//			for (String name : configuration.getMappedStatementNames()) {
			//				logger.debug(name);
			//			}
			logger.warn("刷新mybatis xml配置语句成功");
			return "刷新mybatis xml配置语句成功";
		} catch (Exception e) {
			logger.warn("刷新mybatis xml配置语句失败");
			e.printStackTrace();
			return "刷新mybatis xml配置语句失败";
		}
	}

	public void setPackageSearchPath(String packageSearchPath) {
		this.packageSearchPath = packageSearchPath;
	}

	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}

	/**
	 * 扫描xml文件所在的路径
	 * @throws IOException
	 */
	private void scanMapperXml() throws IOException {
		this.mapperLocations = new PathMatchingResourcePatternResolver().getResources(packageSearchPath);
	}

	/**
	 * 清空Configuration中几个重要的缓存
	 * @param configuration
	 * @throws Exception
	 */
	private void removeConfig(Configuration configuration) throws Exception {
		Class<?> classConfig = configuration.getClass();
		clearMap(classConfig, configuration, "mappedStatements");
		clearMap(classConfig, configuration, "caches");
		clearMap(classConfig, configuration, "resultMaps");
		clearMap(classConfig, configuration, "parameterMaps");
		clearMap(classConfig, configuration, "keyGenerators");
		clearMap(classConfig, configuration, "sqlFragments");
		clearSet(classConfig, configuration, "loadedResources");
	}

	@SuppressWarnings("rawtypes")
	private void clearMap(Class<?> classConfig, Configuration configuration, String fieldName) throws Exception {
		try {
			Field field = null;
			if (configuration.getClass().getName().equals("com.baomidou.mybatisplus.core.MybatisConfiguration")) {
				field = classConfig.getSuperclass().getDeclaredField(fieldName);
			} else {
				field = classConfig.getDeclaredField(fieldName);
			}
			field.setAccessible(true);
			Map mapConfig = (Map) field.get(configuration);
			mapConfig.clear();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	private void clearSet(Class<?> classConfig, Configuration configuration, String fieldName) throws Exception {
		Field field = null;
		if (configuration.getClass().getName().equals("com.baomidou.mybatisplus.core.MybatisConfiguration")) {
			field = classConfig.getSuperclass().getDeclaredField(fieldName);
		} else {
			field = classConfig.getDeclaredField(fieldName);
		}
		field.setAccessible(true);
		Set setConfig = (Set) field.get(configuration);
		setConfig.clear();
	}

	/**
	 * 判断文件是否发生了变化
	 * @return
	 * @throws IOException
	 */
	private boolean isChanged() throws IOException {
		boolean flag = false;
		for (Resource resource : mapperLocations) {
			String resourceName = resource.getFilename();
			boolean addFlag = !fileMapping.containsKey(resourceName);// 此为新增标识
			// 修改文件:判断文件内容是否有变化
			Long compareFrame = fileMapping.get(resourceName);
			long lastFrame = resource.contentLength() + resource.lastModified();
			boolean modifyFlag = null != compareFrame && compareFrame.longValue() != lastFrame;// 此为修改标识
			// 新增或是修改时,存储文件
			if (addFlag || modifyFlag) {
				fileMapping.put(resourceName, Long.valueOf(lastFrame));// 文件内容帧值
				flag = true;
				break;
			}
		}
		return flag;
	}
}
