package qcz.zone.redis.annotation;

import java.lang.annotation.*;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2019 - 11 - 19
 */

/**
 * 自定义类注解（用于辅助AOP快速发现自定义字段注解）
 */
//@Inherited  // 支持继承
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QczUtils {
}
