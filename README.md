Urras
===================================
# 模块   

### urras-parent
    父模块:spring-boot-parent
    所有urras模块的父模块。用于管理版本和依赖包的版本。属于根模块。

### urras-support
    父模块:urras-parent
    基础支撑模块，用于对整个工程共用的对象进行定义（例如本项目通用的 enum，exception）。藉此来拓展一些必要的规范，除parent外，其他模块都必须依赖改模块。
    需要说明的是，并不是所有公用对象都放在这里。一般只放enum。
### urras-tools-kit
    父模块:urras-parent, 依赖：urras-support
    提供一些必要的工具类，尤其是Https
### urras-crud
    父模块: urras-parent, 依赖: urras-support, urras-tools-kit
    提供简单的 CRUD 支持，并依赖 mybatis 和 tk.mapper 相关组件
### urras-web-office-starter
    父模块:urras-parent, 依赖: urras-support, urras-tools-kit, urras-crud
    提供一个简单的，开箱即用的 office-admin 后台基础功能。
    
### 统一修改各模块版本,在urras-dependencies-bom目录下执行
```
mvn versions:set -DnewVersion=2.4-SNAPSHOP
mvn versions:set -DnewVersion=1.2.0.RELEASE
```



# 代码规范
### 基础代码规范
  +  NPE 问题
    - urras极其衍生系统下所有自定义的对象一律不允许做无意义的非空检查。方法中只能对@Nullable声明的参数做null检查，其他一律不允许。
    - 当某个方法的返回值可能为空时，应该直接使用 Optional<T> 进行声明。
    - 字段和方法参数不允许直接使用Optional进行申明，应该使用@Nullable 进行声明，因为这样做还有许多的问题，比如Mybatis O/R映射不支持等等。如果存在可能为空的字段，应该使用类似如下方式声明：
      private String name;
      public void setName(@com.chuang.urras.support.Nullable String name) { this.name = name; }
      public Optional<String> getName() { return Optional.ofNullable(name); }
    - 一切可能为空(Optional或@Nullable)的参数都应该放在非空参数后面，如：void test(String name, Optional<String> nickName, @Nullable msg);
    - 当参数超过4个时，或者参数有可能变动的情况下就应该考虑使用对象作为参数，而不是简单的把参数列出来。
    - 除非Map是特殊的，例如它是request的头信息或者参数集合。否则严禁直接使用Map作为方法的参数和返回值，违者割鸡鸡。
      因为Map的类型太弱了，不读代码根本不知道里面有些什么。这样的接口写了之后很难被复用，也很可能被一些自以为是的人做一些所谓的“增强”，最后让系统维护变得痛苦不已。
      
  + 横向分层问题
    - 分层应该遵循 MVC 结构。 分层方式可以参考 
      core(model,dao,service,polymer)
      web (polymer, controller)
      需要注意 polymer在core和web都有出现，这取决于polymer的业务属于哪个层。关于polymer的说明会在下面给出
    - 所有模块都尽可能的遵循 dao <- service <- controller 的方式进行横向分层。
          层与层之间的依赖必须严格遵循上面的分层结构，严禁循环依赖，如果出现循环依赖就应该仔细思考对策，而不是偷懒。    
    - dao与dao之间，service与service之间，controller与controller之间严禁互相依赖。
      需要注意的是service层往往是业务核心，常常一个业务需要多个service共同协作。即便如此也不应该打破这条规定。
      相关的策略请查看下面的《关于service类型》的描述
  + 关于service的类型，urras规范建议service应该分为如下三种，各司其职。
    - 单一型 service 
        单一型service通常直接继承 BaseService<T> 来实现单一表的增删改查，相当于对Mapper进行业务上的增强。
        这类service严禁依赖其他任何service。严禁提供任何级联查询接口。它只提供单一表的查询。通常是用户系统使用较多 
    - 统计型 service 
        这类service通常提供统计，允许进行级联查询，通常给office，admin一类系统使用，用户系统应该尽量的少调用，甚至不调用。
    - 聚合型 service
        当某个业务需要多个表的数据时，单一型service就无法满足需要了。这时候就需要聚合性service（通常命名为polymer）来满足需要
        需要说明的是聚合型service不允许直接依赖dao，而是依赖统计和单一型service。对它们的功能进行聚合以完成复杂的业务处理。
    - 除聚合性 service外，其他两类service严禁依赖其他service.
  + 关于以上service分层的优势
    - service之间不允许相互引用，减少了service的复杂度，重复的且简单的查询可以通过BaseService提供统一实现，避免N个人写N个一样功能的代码。
    - 反对级联查询，并坚持使用多次单表查询并通过程序自行组合对象的原因是，它从理论上会降低程序性能，但实际上却增加了。
      原因是，当用户系统中某个数据的查询成为热点数据，且已经对系统的性能造成了瓶颈。单一型service可以非常轻松的提供缓存支持。由于是单一的service，不用考虑缓存外溢的情况。不用考虑缓存后存在脏数据问题。
      我们总是在系统有瓶颈时希望快速解决瓶颈，而不是对未知的，几乎忽略不计的问题抓着不放。而当真正的瓶颈出现时又没有快速解决的手段。实际上级联查询常常导致这样的问题。这是我们痛恨它们的原因。
      而且级联查询常常也因为大表级联和不利于维护被DBA们诟病。把级联查询当做二等公民，让大家都轻松点。 
    - 统计类service明确的独立出来有助于引起调用者的注意（它可能是一个耗时的查询，在撰写用户系统代码时需要注意它是否应该被调用。是否存在其他更好的办法?）
    
  + 纵向分模块问题       
    - 纵向模块不同于工程内部的横向切分（如：core，web）
      如果横向分层是根据职能切分，并通过依赖建立各职能之间的关系。纵向分模块就应该是按照业务区分，并通过依赖建立各业务之间的联系。
      于横向分层不同，纵向分模块往往和远程调用，分布式问题结合在一起。
  + 分层问题综述
    - 必须非常明确的指出，urras规范坚决反对横向分层的分布式策略。我们始终认为这是一种愚蠢地，百害无一利的策略。
      就代码（服务）共用来说，横向分层完全可以通过pom模块分层后使用jar来依赖。按照以往的经验，横向分层的分布式策略
      很容易让系统变得复杂（不同功能的系统被交织在一起，系统会随着业务的增加而使复杂度程指数上升）
    - 我们推荐纵向分模块的分布式策略（如微服务），横向分层只在单一系统中使用。但同时需要注意的是，由于业务的不可预知性。
      纵向的分布式策略依然有可以出现循环依赖，我们应该尽可能的在业务设计上仔细思考以避免这种情况的发生。不要偷懒，不要偷懒，不要偷懒。
    - 纵向的分布式策略可以使用spring-cloud或其他类似技术来弥补一些使用上的复杂性。
  
  + 严格分层后的一些问题和解决之道。
    - 某些情况下层的业务可能会反向依赖上层业务，这是有可能的。比如当你想要写一个游戏平台的开发包时，你希望建立一个标准化的 game-sdk 接口模块。然后提供给core模块使用。
      core模块不再需要关心调用游戏接口的问题。game-sdk将提供统一的，标准的API来访问所有的平台。这是一个不错的思路。但是在开发过程中你可能发现每一个平台都有一些参数是需要通过配置来获取的。
      比如游戏平台的商户号，秘钥。而商户号和秘钥可能保存在数据库中（许多时候可能出于管理的方便必须保存在数据库中）。但数据的访问都在core模块中。
      这样就出现了循环依赖。core依赖于game-sdk 这是我们一开始的目的。但现在game-sdk却出现了反向依赖core模块的数据库资料。我们需要避免它的发生。
      一个很错误的解决方案是在game-sdk中维护一个properties文件来提供配置。这样是愚蠢的。因为配置出现了两份，一份是动态可维护的，一个是静态的，必须重新编译。
      一个较好的做法是在game-sdk中提供一个获取配置的接口，如 interface GameConfigProvider { GameConfig getConfig(String platform);}
      然后由core模块去实现并查询数据库。这样就能能摆脱循环调用的厄运了。
    
  + 当使用webservice之类的远程调用时
    - 严禁直接将core模块的接口放出去，因为这样别人不得不依赖你整个core，以及core依赖的其他jar
    - 应该创建一个新的模块，如 remote-api 模块，remote-api 中只能包含模块内声明的对象，service和枚举。不允许再有其他任何其他依赖，让调用者可以直接使用api的jar
    - 严禁core模块直接实现remote-api模块的接口，因为这样导致core必须依赖remote-api。而应该创建一个remote-impl模块同时依赖 remote-api 和 core模块。
    - 严禁为了偷懒而直接让 remote-api 依赖 core模块，直接使用core模块定义的model，service。这样将同第二点的后果一样。

  + 代码风格
    - 数据查询方法统一用select, find开头，为了调用者在使用service的查询功能时被一堆查询方法搞迷糊，默认用户自己实现的查询都要 find* 开头
    - 所有 Qry 表示从前台传递过来的参数对象，通常参数比较少，且可能需要进行参数验证。
    - 所有 VO 表示要发送到浏览器的数据对象。 出管理员系统允许直接把Entity传递到浏览器外，其他一律用DO来传递，避免entity增加敏感信息后被误传到浏览器。
    - Qry 和 VO 只能出现在 web 层中。 core、api、impl中不能使用。
    - VO 存在的原因是：Entity包含了所有的信息，其中可能存在敏感信息。不推荐通过修改sql的字段列表来防止敏感信息被传到前端（太过麻烦，代码不够清晰）。
                    因此这种情况下才需要创建相关的 VO。
    - Qry 存在的原因是：前台参数某些时候会比较长，例如修改个人资料时，如果将个人资料的参数全部列举会导致参数列表过多。所以有必要使用对象作为参数。
                    这里不推荐使用Entity，因为Entity中的字段不一定是必要的。这样会使得日后维护时，无法确定那些参数有用，那些参数没用。
    - 注意：Qry和VO不是简单的 Entity 拷贝。 VO只保存Entity作为数据时的非敏感信息。 Qry 只在参数列表过大时创建，并且只保存查询或更新的参数。
    
# 关于shiro的几个优化
 ### shiro 和 druid 矛盾。 
    需要说明的是，系统中使用 druid 和 shiro。由于某些特定的路径下的请求在系统配置中不进行user验证，直接放行。
    当一次请求结束后，shiro会自动判断用户是否已经退出登录，如果是则删除session。这带来一个问题，不进行user验证的所有请求，在执行完成后，都被shiro当做退出登录了。
    因此session会被自动删除。而 druid 配置启动数据库请求监听时 filter 需要获取session。当获取session失败就会报错。这对业务似乎没有影响。
    但似乎springmvc会因为报错而将response的返回码变成 500，而不 200。 而有些客户接入我们的接口时可能会比较严格的判断返回码必须为200。
    这个锅应该shiro背。解决这个问题的方式是通过第二个优化方案(shiro 和 redis矛盾)顺带解决的。
 ### shiro 和 redis 矛盾
    shiro 默认每次获取session都会通过cacheManager中的sessionDao去获取。我们系统中cacheManager设置的是redis实现。那么每一次请求中一大堆的filter都会去getSession()
    这导致一次请求shiro就丧心病狂的访问了redis 无数次。解决这个问题的方式参考MyShiroSessionManager实现的说明。正好这种解决方法对shiro和druid问题也有神奇疗效。
    不过这锅大概shiro不背。如果shiro一次访问中只从redis中获取一次session，那么实际上其他服务器共享session时修改的数据，就无法及时获取（不过这场景很少）。

# shiro的坑
    1. shiro 创建session时，会同时调用update session，会连续两次访问redis，如果session有大量的更新，也会多次访问redis
    2. shiro 删除session时也有类似问题
        protected void onExpiration(Session s, ExpiredSessionException ese, SessionKey key) {
                log.trace("Session with id [{}] has expired.", s.getId());
                try {
                    onExpiration(s); // 检查走change并删除session
                    notifyExpiration(s);
                } finally {
                    afterExpired(s); // 删除session
                }
            }

# 如果系统不是rest风格
## js规范
### main.js 中的代码是所有页面都会用到的。通常是一些规范和协议。例如：
 + 所有验证码name都使用 captcha 。 例如  <input name="captcha" >
 + 凡 Ajax 返回的结果，都是Result结构。Result的结构说明可以参考main.js的注释。
 + 凡 带有"captcha-js"样式的元素都默认添加 点击 刷新验证码的功能，且所有"captcha-js"样式的元素验证码会随之一起变更。
     例如同一页面有多个验证码，且都使用"captcha-js"样式。那么任意一个验证码点击刷新后，所有验证码同时变更。
 + 所有带有 kf-js 样式的元素都会默认添加 点击 弹出客服窗口的功能。
 + 所有带有 ajax-form 样式的form表单元素，默认都将submit转换为ajax提交。并对Ajax得到的结果进行默认处理。处理方式参考第 二 条。
     另外，如果form中包含 name为captcha 的元素，则会自动刷新验证码(验证码规定参考第一条)
     
# 关于项目中对缓存的使用、缓存对象的关系如下：
    本项目中的缓存接口为 ICached，其实现为 RedisCachedImpl. 该接口属于 Hmap，具体实现为 RedisTemplate。template 则真正实现了数据源，连接池的配置。
    注意: 现在不打算为redis做异步或反应式实现。以后大概也不会做。原因是“没必要”。redis已经足够快。而且相对与PaymentSDK中对第三方接口的调用来说内外部署的redis其实非常稳定，使用异步反而变得特别复杂。
    数据缓存并没有使用到 ICached 接口，而是有spring cache全权管理。
    ICached在shiro的缓存系统中有被使用到，shiro的缓存系统将一个Cache单纯作为一个类似map的对象使用。而多个Cache有一个CacheManager进行管理。
    shiro中为了保证Cache不被频繁创建(shiro认为Cache的创建可能涉及到创建连接)，CacheManager内部会通过Map以键值 cacheName/Cache 的形式保存已经创建的Cache对象。
    本项目中的Cache实现始终引用同一个ICached实现。理由是ICached本身就支持 hget(cachename, key)的方式来获取value。即便在shiro不对Cache对象进行缓存时，也不会重复创建连接。
# 误区：
    ICached 是一个hmap缓存，和shiro中的Cache不完全一样，ICached更像是shiro中的CacheManager + Cache
    sessionManager默认使用的是CacheingSessionDAO，内部使用CacheManager来实现session缓存。
    在shiro的框架下实现的CacheManager(包括本项目中的ShiroRedisCacheManager)都在内部维护了一个ICached引用的缓存。
    避免每次都去创建cache，浪费连接。（之前认为的session会在本地保存一份是错误的，实际上缓存只是为了缓存真实的cache引用）
    避免多次创建cache（某些cache创建可能需要创建连接等等。）
    缓存的cache是活动cache。当用于离线后，cache实际是没有被清理的，这时候就需要调度来清理。
