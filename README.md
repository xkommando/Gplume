Gplume
======

Light-weight multiparadigmatic Java web framework.

Features:
 - easy to learn and use
 - mixed Spring styled and Struts2 styled request handling
 - high performance.
 - cross cloud.

----------------------------
Quick Start:
#####Part Zero: maven dependency
```XML
<dependency>
	<groupId>com.caibowen.gplumeframwork</groupId>
	<!-- web developement extensions-->
	<artifactId>gplume-webex</artifactId>
	<version>1.0</version>
</dependency>
```
#####Part One: Configurate the web.xml:

```XML
<context-param>
	<param-name>manifest</param-name>
	<param-val>classpath:app-manifest.xml</param-val><!-- in your class path -->
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
#####Part Two: The IoC Container.
Spring-like configuration:
```XML
<beans namespace="MyApp::internal::storage" using="internal::api">

  <config>some_other_config.xml</config>
  <config>so_many_configs.xml</config>

<!--add global properties-->
  <properties import="kv_pairs.properties" scope="global">
    <gplumeVersion>1.0.0.nightly</gplumeVersion>
    <webErrorHandler>com.caibowen.web.misc.ErrorHandler</webErrorHandler>
  </properties>

<!-- default scope: current file -->
  <properties import="kv_pairs_of_xml_format.xml" scope="file"/>
<!--all properties is accessible at run time-->

  <bean class="com.caibowen.gplume.web.WebConfig" aftercall="afterPropertySet">
    <construct> <!--constructor injection -->
        <list>
            <bean class="some.class"/>
            <ref>MyApp::OtherNS::someOtherBean</ref>
            <val>literal</val>
        </list>
    </construct>
    <field name="preProcessor" ref="headPrePrcessor"/>
    <field name="errorHandler" instance="${webErrorHandler}"/>
    <field name="pkgs">
        <list>
            <val>com.caibowen.web.controller</val>
        </list>
    </field>
  </bean>
<!-- support dynamic proxy-->
  <bean class="com.sample.MyInterface" proxy="com.sample.MyInvokeHanlder">
    <construct name="what is this" value="invokeHanlderConstructor"/>
  </bean>
<beans>
```
#####Part Three: Internationalization. 
add language packages 
	`en.properties` where there is `gplumeIsRunning=Gplume is Running!` 
and 
	`zh_CN.properties` where there is `gplumeIsRunning=Gplume \u8DD1\u8D77\u6765\u4E86\uFF01`(ascii for `Gplume跑起来了`)  

specify them in the manifest.xml
```XML
<!-- hot swap service. in production mode, use WebI18nService-->
<bean id="i18nService" class="com.caibowen.gplume.web.i18n.HotSwapWebI18n">
	<construct>
		<map>
			<en value-type="String">${lang_cn}</en>
			<zh_CN>/${i18base}/${i18_cn}</zh_CN>
		</map>
	</construct>
	<field name="defaultLang" value="SimplifiedChinese"/>
	<field name="${time_zone_name}" value="ETC/GMT-8"/>
</bean>
```
use message tag in JSP
```HTML
<label>
	<gp:msg k="gplumeIsRunning" />
</label>
```
#####Part Four: Gplume JDBC Operations
``` Java
jdbcSupport.setDataSource(dataSource);
List<String> ids = jdbcSupport.batchInsert(new StatementCreator() {
    @Override
    public PreparedStatement createStatement(@Nonnull Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO `model_talbe`(`id, `name`)VALUES (?,?)");
        for (Model model : models) {
            ps.setString(1, model.getId());
            ps.setString(2, model.getName());
            ps.addBatch();
        }
        return ps;
    }
}, new String[]{"id"}, RowMapping.STR_ROW_MAPPING) ;

List<Model> ls = jdbcSupport.queryForList(new StatementCreator() {
        @Override
        public PreparedStatement createStatement(@Nonnull Connection con) throws SQLException {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `table_model`");
            return ps;
        }
    }, new RowMapping<Model>() {
        @Override
        public Model extract(@Nonnull ResultSet rs) throws SQLException {
            Model model = new Model();
            model.setId(rs.getString(1))
            model.setName(rs.getString(2));
            return model;
        }
    });

jdbcSupport.execute(new TransactionCallback<Object>() {
    @Override
    public Object withTransaction(@Nonnull Transaction tnx) throws Exception {
        tnx.setRollbackOnly(true);
        // operation 1
        // operation 2
        jdbcSupport.execute(new TransactionCallback<Object>() {
            @Override // nested transaction is isolated from the outer one
            public Object withTransaction(@Nonnull Transaction tnx) throws Exception {
                tnx.setRollbackOnly(true);
                throw new Exception();
            }
            });
            // fail on any of the operations will trigger rollback automatically
        return null;
    }
});
```
#####Part Five: ORM using Hibernate.
Spring and Hibernate can be integrated to Gplume with just a few lines configuration:

``` XML
<!-- will set sessionFactory bean with id ${sessionFactoryID} in afterPropertiesSet() -->
<bean class="com.caibowen.gplume.sample.test.SessionFactoryBuilder">
	<field name="sessionFactoryID" value="sessionFactory"/>
	<field name="dataSource" ref="dataSource" />
	<field name="hibernateProperties">
		<map>
			<hibernate.dialect>org.hibernate.dialect.MySQL5Dialect</hibernate.dialect>
		</map>
	</field>
	<field name="annotatedClasses">
		<list> <!-- set field "Class[] annotatedClasses" -->
			<val>com.caibowen.gplume.sample.model.Chapter</val>
		</list>
	</field>
</bean>
<!-- use HibernateDaoSupport-->
<bean id="chapterDao" class="com.caibowen.gplume.sample.dao.ChapterDAO">
	<field name="sessionFactory" ref="sessionFactory" />
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
#####Part Six. Handling Event. 
register listeners and publish events as:
```Java
AppContext.broadcaster.register(new IEventHook() {
@Override
public void catches(AppEvent event) {
		LOG.info("cought event {0} from source {1}"
				, event.getClass().getSimpleName()
				, event.getSource());
	}
}, false);//set false to keep a weak reference to this listener
AppContext.broadcaster.register(new IAppListener<TimeChangedEvent>() {
@Override
public void onEvent(TimeChangedEvent event) {
		LOG.info("time changed {0}", event.getTime());
	}
});// default is true
TimeChangedEvent event = new TimeChangedEvent(this);
event.setTime(new Date());
AppContext.broadcaster.broadcast(event);
```
#####Part Seven. Test with Junit
```Java
@RunWith(JunitPal.class)
@ManifestPath("file:src/manifest.xml") // read as file
public class TestDemo {
	@Named("controlCenter")
	public AbstractControlCenter controlCenter;
	@Test
	public void test() {
		System.out.println(controlCenter);
		AppContext.broadcaster.broadcast(new WebAppStartedEvent(this));
	}
}
```

#####Part Eight: Gplume Web MVC
Pre-process and after-process requests
```java
public class SampleProcessor implements IRequestProcessor {
	@Override // all requests go through this
	public void process(RequestContext context) {
		before(context);
		getNext().process(context);
		after(context);
	}
```
Chaining processors in XML
```XML
<bean id="headPrePrcessor" class="com.caibowen.gplume.web.i18n.NativePkgInjector">
	<construct ref="i18nService"/>
	<field name="next">
		<bean class="com.caibowen.web.plugin.ui.BackgroundImgPreprocessor">
			<field name="next" instance="com.caibowen.web.plugin.statistic.LogRequestPreProcessor"/>
		</bean>
	</field>
</bean>
<!--config view resolver-->
<bean class="com.caibowen.gplume.web.WebConfig">
	<field name="preProcessor" ref="headPrePrcessor"/>
	<field name="pkgs">
		<list>
			<val>com.caibowen.gplume.sample</val>
		</list>
	</field>
	<!--by default using Jsp view resolver-->
	<field name="viewPrefix" value="/"/>
	<field name="viewSuffix" value=".jsp" />
	<!-- IView Resolver -->
	<field name="ViewResolver"/>
		<bean class="com.caibowen.gplume.webex.json.JsonViewResolver">
			<field name="doPrettyPrint" value="true"/>
		</bean>
	</field>
</bean>

```
Handle request with method
```Java
// Support arbitrary return type. Customer view resolvers is binded before runing
class MyStrViewResolver implements IViewResolver;
class FreeMarkerResolver implements IViewResolver;
```
```Java
// controller
@Handle(value={"/", "/index",
	"/index.html", "/index.jsp"}})
public String index(RequestContext context) {
	context.putAttr("msg", nativeStr("gplumeIsRunning", context));
	return "index"; 
}
	//number of concurrent requests is limited by this semapher
@Semaphored(permit=100, fair=false, timeout=1000)// when time is out, send http code 503 
@Handle(value={"/your-birthday/{date formate like 1992-6-14::Date}"}
	, httpMethods={HttpMethod.GET, HttpMethod.POST})
public FreeMarkerView happyBirthday(Date date) {
	process(date);
	return new FreeMarkerView(date);
}
```
Handle request with an object storing current state
```Java
//Warn: insecure login controller, for demo only
@Controller("/async/")// the base path
public class MyAction {

	@ReqAttr(value="alias", defaultVal="1992-6-14")
	Date testdata1;
	@CookieVal(defaultVal=" 2.457  ", nullable=false)
	double testdata2;
	@ReqParam("psw_cipher")
	String passwordCipher;
	@ReqParam(value="email_address", nullable = false)
	String email;
	@SessionAttr(value="this_pubkey",nullable = false)
	PublicKey key;

	@Inject // injected after instanciation 
	@Named("myUserService")
	UserService userService;
	@Inject Validator validator;
	@Inject PublicKeyService keyService;

	User user;
	boolean ok() {
		String psw = keyService.decrypt(key, passwordCipher);
		if (!Str.Utils.notBlank(psw)
				||!validator.matches(email, psw))
			return false;
		else {
			user = userService.getByEmail(email);
			return true;
		}
	}
	
	@Semaphored(permit=300, timeout=2200)
	@Handle(value={"login"}, httpMethods={HttpMethod.POST})
	public SomePoJo login(MyAction reqScope, RequestContext req) {
		if (reqScope == null) //non-null requirements are not met.
			return IView.get.textView("no public key in session");
		else if (!reqScope.ok())
			return Views.textView("password and email mismatch");
		else {
			req.session(true).setAttribute("this-user", reqScope.user);
			return Jump.to("/user/" + reqScope.user.getNameURL());
		}
	}
}
```
Intercept requests
```java
@Intercept(value = { "/user*" }) // intercept request by URL
public void demo(RequestContext context, IAction action) throws Throwable {
	if (hasLoggedIn(context)) {
		action.perform(context);
		after(context);
	} else {
		context.jumpTo("/login");
	}
}
```

===========
**************
Gplume Overview
![alt text](https://dl.dropboxusercontent.com/s/iklpmr1jdyktdn2/gplume_struture.jpg)

***************
Project home www.caibowen.com/work.html#id_gplume
