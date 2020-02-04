package qcz.zone.redis.annotation;

import qcz.zone.redis.constant.RedisDBIndex;

import java.lang.annotation.*;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2019 - 11 - 19
 */

/**
 * 自定义字段注解（动态注入自定义QczRedisTemplate）
 */
//@Inherited  // 支持继承
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QczRedisAnnotation {
    // redis数据库index
    RedisDBIndex dbIndex() default RedisDBIndex.DB_INDEX_NULL;
}
