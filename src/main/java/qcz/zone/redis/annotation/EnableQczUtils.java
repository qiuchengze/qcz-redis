package qcz.zone.redis.annotation;

import org.springframework.context.annotation.Import;
import qcz.zone.redis.aspect.AspectRedisTemplate;

import java.lang.annotation.*;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2019 - 12 - 21
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(value = {AspectRedisTemplate.class})
public @interface EnableQczUtils {
}
