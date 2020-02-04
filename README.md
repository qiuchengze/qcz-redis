# redis-template
自定义封装的一个redis模板
特点：
1. lettuce连接池；
2. fastjson对象序列化；
3. db0~db15可直接通过自定义类注解（@QczUtils）+自定义字段注解（@QczRedisAnnotation）动态注入使用；
4. 简化操作命令（逐步增加和完善）

使用实例（启动类需添加@EnableAspectJAutoProxy注解（开启AOP））：

package com.qcz.utils.redis.controller;

import com.qcz.utils.redis.annotation.QczRedisAnnotation;

import com.qcz.utils.redis.annotation.QczUtils;

import com.qcz.utils.redis.constant.RedisDBIndex;

import com.qcz.utils.redis.template.QczRedisTemplate;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RestController;


/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.qcz.zone
 * @create: 2019 - 11 - 19
 */


/**

 * 测试用例
 
 */
 
/*========== 启动类需添加@EnableAspectJAutoProxy注解（开启AOP）==========*/

/*========== 自定义类注解(在使用此@QczRedisAnnotation注解的类上必需加上此注解，否则自定义AOP无法动态注入) ==========*/

@QczUtils

@RestController

public class TestController {

    /*========== 使用自定义注解动态注入相应index的redis数据库模板 （有效数字 0 ~ 15）==========*/
    
    /**
    
     * 使用方法：
     
     *
     
     * eg1（成员变量方式）：直接使用带尾数字的成员变量名（末尾数字必需为 0~15），成员变量末尾如未带数字默认为0，
     
     * 末尾的数字即为需要注入的对应index的redis数据库模板。
     
     * 此方式优先级低于注解属性方式（两种方式都使用时，以注解属性试为准）
     
     *
     
     * eg2（注解属性方式）：直接设置自定义字段注解属性（dbIndex）值（取值范围为枚举 RedisDBIndex.DB_INDEX_0 ~ RedisDBIndex.DB_INDEX_15，
     
     * 此属性默认值为：DB_INDEX_NULL，此值为强制使用成员变量方式），
     
     * 注解属性（dbIndex）值即为需要注入的对应index的redis数据库模板。
     
     * 此方式优先级高于成员变量方式（两种方式都使用时，以此方式为准）
     
     */
     
    @QczRedisAnnotation
    private QczRedisTemplate qczRedisTemplate;
    
    @QczRedisAnnotation
    private QczRedisTemplate qczRedisTemplate0;
    
    @QczRedisAnnotation
    private QczRedisTemplate qczRedisTemplate1;
    
    @QczRedisAnnotation(dbIndex = RedisDBIndex.DB_INDEX_1)
    private QczRedisTemplate qczRedisTemplate2;
    
    @QczRedisAnnotation(dbIndex = RedisDBIndex.DB_INDEX_NULL)
    private QczRedisTemplate qczRedisTemplate3;
    
    @QczRedisAnnotation(dbIndex = RedisDBIndex.DB_INDEX_4)
    private QczRedisTemplate qczRedisTempl;
    
    @GetMapping("/test")
    public void test() {
    
        // 返回当前redis数据库index
        
        System.out.println(qczRedisTemplate.getIndex());    // db：0
        
        System.out.println(qczRedisTemplate0.getIndex());   // db：0
        
        System.out.println(qczRedisTemplate1.getIndex());   // db：1
        
        System.out.println(qczRedisTemplate2.getIndex());   // db：1
        
        System.out.println(qczRedisTemplate3.getIndex());   // db：3
        
        System.out.println(qczRedisTempl.getIndex());       // db：4
        
    }
}
