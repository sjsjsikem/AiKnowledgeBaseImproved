package com.aiknowledgebase.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatisPlusConfig 是 MyBatis-Plus 通用配置类。
 * 它注册 MyBatis-Plus 自带分页拦截器，在本项目中让 Page<User> 这类分页查询真正生成数据库分页 SQL。
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * mybatisPlusInterceptor 创建 MyBatis-Plus 插件容器。
     * 它把 PaginationInnerInterceptor 加入插件链，在本项目中支撑后台用户列表和后续知识库列表分页。
     *
     * @return MybatisPlusInterceptor 来自 MyBatis-Plus，用于挂载分页等内部拦截器。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
