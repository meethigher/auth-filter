Java基于SpringBoot框架的常用三种授权方式

1. filter
   * 好处
     1. 不影响前端页面的映射
   * 坏处
     1. 不支持Spring的全局异常捕获。Filter链执行完之后才会到Servlet，故使用Filter无法经过Spring的全局异常捕获，且无法丢自定义异常
     2. 只支持匹配规则，不支持排除规则。需要自己实现
2. spring-mvc-configuration-support
   * 好处
     1. 权限较大。可以拦截整个SpringWEB的路由，进行映射
     2. 支持匹配与排除规则
     3. 支持Spring的全局异常捕获(本质是基于Spring的DispatcherServlet内部实现的)
   * 坏处
     1. 页面映射需要自定义。比如swagger、部分spring配置参数等
3. spring-mvc-configurer
   * 好处
     1. 不影响前端页面的映射
     2. 支持匹配与排除规则
     3. 支持Spring的全局异常捕获((本质是基于Spring的DispatcherServlet内部实现的))
   * 坏处-暂无



`WebMvcConfigurationSupport` 和 `WebMvcConfigurer` 都是 Spring MVC 中用于配置和自定义 Spring MVC 的组件，但它们有一些区别。

1. `WebMvcConfigurationSupport`： `WebMvcConfigurationSupport` 是一个抽象类，它提供了一个基本的 Spring MVC 配置，并且可以被继承来自定义更复杂的配置。如果你需要对 Spring MVC 进行更深入的定制，可以继承这个类，并覆盖其中的方法来配置拦截器、视图解析器、格式化器等。通过继承 `WebMvcConfigurationSupport`，你可以完全控制 Spring MVC 的配置，但同时需要自己处理更多的细节。
2. `WebMvcConfigurer`： `WebMvcConfigurer` 是一个接口，它提供了一组回调方法，允许你在 Spring MVC 的配置阶段进行自定义操作，而不需要继承任何类。通过实现这个接口，你可以注册拦截器、配置视图解析器、资源处理器、数据格式化器等。使用 `WebMvcConfigurer` 接口，可以更加简洁地实现一些配置需求，而不需要创建一个新的配置类。

总的来说，`WebMvcConfigurationSupport` 是用于更复杂的配置情况，需要继承并重写方法，可以对 Spring MVC 进行全面的定制。而 `WebMvcConfigurer` 是一种更简洁的方式，通过实现接口来实现一部分配置，适用于较为简单的定制需求。

一般来说，如果你需要对 Spring MVC 进行大量的自定义配置，推荐使用 `WebMvcConfigurationSupport`；而对于简单的配置需求，`WebMvcConfigurer` 更加方便和推荐。在实际使用中，你也可以将两者结合起来，以满足不同层次的配置需求。