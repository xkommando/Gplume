Gplume
======

light-weight Java web framework that cross-cloud

Quick Start:

**Step Zero**. Prepare. 
in your web.xml:

```XML
	<context-param>
        <param-name>manifest</param-name>
        <param-value>/WEB-INF/app-manifest.xml</param-value>
    </context-param>
	<listener>
		<listener-class>com.caibowen.gplume.web.WebAppBooter</listener-class>
	</listener>
		<filter>
		<filter-name>dispatcher</filter-name>
		<filter-class>com.caibowen.gplume.web.GFilter</filter-class>
	</filter>
	<filter-mapping>
		<filter-name>dispatcher</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
```
**Step One**. IoC Container. 
Write app-manifest.xml, it is similar to spring applicationContext.xml :

```XML
	<bean id="i18nService" class="com.caibowen.gplume.web.i18n.WebI18nService">
	    <property name="defaultLang" value="SimplifiedChinese"/>
	</bean>
	<bean id="bootHelper" class="com.caibowen.gplume.web.i18n.WebAppBootHelper">
	    <property name="pkgFiles">
	        <list>
	            <value>/WEB-INF/i18n/en.properties</value>
	            <value>/WEB-INF/i18n/zh_CN.properties</value>
	        </list>
	    </property>
	    <property name="defaultTimeZone" value="ETC/GMT-8"/>
	</bean>
```
**Step Two**. Web. 
A web Controller is like:

```Java
	@Handle(value={"/Gplume/",
			"/Gplume/index",
			"/Gplume/index.html",
			"/Gplume/index.jsp"},
			httpMethods={HttpMethod.GET, HttpMethod.POST})
	public void index(RequestContext context) {
		
		context.putAttr("msg", nativeStr("gplumeIsRunning", context));
		context.render("/index.jsp");
	}
```
**Step Three**. Internationalization. 
Lets get cultural! For i18n, you need language packages like  
`en.properties` where there is `gplumeIsRunning=Gplume is Running!` 
and  
`zh_CN.properties` where there is `gplumeIsRunning=Gplume \u8DD1\u8D77\u6765\u4E86\uFF01`(ascii for `Gplume跑起来了`)  
and specify them in the manifest.xml

**Step Four**. Event. 
There is a Broadcaster, you can register listeners and publish events like:

```Java
AppContext.broadcaster.register(new IEventHook() {
	@Override
	public void catches(AppEvent event) {
		LOG.info("cought event[" + event.getClass().getName() + "]"
				+ "from source[" + event.getSource() + "]");
	}
});
AppContext.broadcaster.register(new IAppListener<TimeChangedEvent>() {
	@Override
	public void onEvent(TimeChangedEvent event) {
		LOG.info("time changed event");
	}
});
TimeChangedEvent event = new TimeChangedEvent(this);
event.setTime(new Date());
AppContext.broadcaster.broadcast(event);
```

**Step Five**. ORM. 
Spring and Hibernate can be integrated with Gplume, just add few more lines in the xml:

``` XML
<bean id="datasource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
	<property name="driverClass" value="com.mysql.jdbc.Driver" />
	<!-- and other properties -->
</bean>
<!-- will set bean sessionFactory in afterPropertiesSet() -->
<bean id="sessionFactoryBuilder" class="com.caibowen.gplume.sample.test.SessionFactoryBuilder">
	<property name="dataSource" ref="dataSource" />
	<property name="hibernateProperties">
		<props>
			<prop key="hibernate.dialect">org.hibernate.dialect.MySQL5Dialect</prop>
		</props>
	</property>
	<property name="annotatedClasses">
		<list>
			<value>com.caibowen.gplume.sample.model.Chapter</value>
		</list>
	</property>
</bean>
<!-- use HibernateDaoSupport-->
<bean id="chapterDao" class="com.caibowen.gplume.sample.dao.ChapterDAO">
	<property name="sessionFactory" ref="sessionFactory" />
</bean>
```
 and the ChaperDAO 
```Java
public class ChapterDAO extends HibernateDaoSupport {
	public List<Chapter> getAll() {
		return getHibernateTemplate().find("from Chapter");
	}
}
```
**************
Gplume Overview
![alt text](https://dl.dropboxusercontent.com/s/eb07qh9ypr24fmi/gplume_structure.jpg)

***************
For more infomation, goto www.caibowen.com/blog/tag/pglume

Project can be find at www.caibowen.com/work.html#id_gplume
