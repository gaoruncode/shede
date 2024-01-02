## 说明
`本程序功能为刷新项目中mybatis xml文件而无需重启项目`
- log使用 apache log4j
- 内置控制器为`xxxxxxxx/mybatis/refersh/{mapping-mysql-*.xml}`
  - get 或 post请求
  - 读取位置为 resources下的xml，不限层级，可通过参数指定 
  - 参数固定格式 xxx-xxx-xxx 层级以`-`分割
### 使用jar，操作步骤
- 添加jar包，路径在本项目 out/artifacts/reloadMybatisXML
- 启动类添加包扫描 `@ComponentScan(value = {"com.gaorun"})`
- 拦截器排除
- 例：http://localhost:8080/user/mybatis/refersh/mapping-mysql-*.xml

### 不使用jar，直接使用源码
- XMLMapperLoader 为核心类
- 添加XMLMapperLoader到项目中
- 方式一 在启动类以Bean方式注入
```java
@Autowired
private SqlSessionFactory sqlSessionFactory;
@Bean
public XMLMapperLoader xMLMapperLoader() {
	return new XMLMapperLoader(sqlSessionFactory,"classpath:mapping/*.xml");
}
 ```
- 方式二 自定义controller
```java
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
```
