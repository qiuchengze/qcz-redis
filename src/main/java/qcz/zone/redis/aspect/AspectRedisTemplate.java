package qcz.zone.redis.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Component;
import qcz.zone.redis.annotation.QczRedisAnnotation;
import qcz.zone.redis.constant.RedisDBIndex;
import qcz.zone.redis.factory.QczRedisConnectionFactory;
import qcz.zone.redis.template.QczRedisTemplate;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2019 - 11 - 19
 */

/**
 * 使用AOP拦截自定义类注解（@QczUtils)，
 * 通过反射扫描发现自定义字段注解（@QczRedisAnnotation)，
 * 并动态注入自定义对象（QczRedisTemplate）bean。
 */
@Aspect
@Component
@ConditionalOnClass(RedisOperations.class)
@EnableConfigurationProperties(RedisProperties.class)
public class AspectRedisTemplate {
    // qczRedisTemplate缓存池
    private static final Map<Integer, QczRedisTemplate> mapQczRedisTemplate = new ConcurrentHashMap<Integer, QczRedisTemplate>();

    // redis配置
    @Autowired
    private RedisProperties redisProperties;

    // 类注解（@QczUtils）切入点
    @Pointcut(value = "@within(qcz.zone.redis.annotation.QczUtils)")
    public void qczUtils() {}

    // 前置通知
    @Before("qczUtils()")
    public void before(JoinPoint joinPoint) {
        // 获取引用类注解（@QczUtils）的目标类
        Class<?> clazz = joinPoint.getSignature().getDeclaringType();

        if (null == clazz) return;

        try {
            Field[] fields = clazz.getDeclaredFields();
            if (null == fields || 0 == fields.length) return;

            // 获取引用类注解（@QczUtils）的目标对象（实例bean），用于动态注入自定义成员对象
            Object obj = joinPoint.getTarget();

            for (Field field : fields) {
                // 当前字段是否存在自定义字段注解（@QczRedisAnnotation)
                if (!field.isAnnotationPresent(QczRedisAnnotation.class)) continue;

                // redis数据库index（有效值 0 ~ 15）
                int index = 0;

                // 当前字段变量名
                String beanName = field.getName();
                // 截取当前字段变量名末尾的数字，如果存在的话，此数字将作为redis数据库index
                String strIndex = beanName.replaceAll(".*[^\\d](?=(\\d+))","");   // 此方法，当末尾没有数字时会返回原字符串
                index = toInt(strIndex);

                // 获取自定义字段注解（@QczRedisAnnotation)参数dbIndex的值，如果存在的话，此数字将作为redis数据库index
                // 此值优先级高于通过字段变量名截取方式
                RedisDBIndex dbIndex = field.getAnnotation(QczRedisAnnotation.class).dbIndex();
                if (RedisDBIndex.DB_INDEX_NULL != dbIndex)
                    index = dbIndex.getValue();

                // 如果库index无效，则默认指向db0
                if (index < RedisDBIndex.DB_INDEX_0.getValue() || index > RedisDBIndex.DB_INDEX_15.getValue())
                    index = 0;

                // 根据redis数据库index从缓存中提取一个template注入给当前字段
                QczRedisTemplate template = getTemplate(index);
                if (null != template) {
                    field.setAccessible(true);
                    field.set(obj, template);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据index从缓存中取出template，如果不存在则创建并加入缓存
     * @param index
     * @return      返回自定义redis模板
     */
    private QczRedisTemplate getTemplate(int index) {
        QczRedisTemplate template = null;
        if (mapQczRedisTemplate.containsKey(index)) {
            template =  mapQczRedisTemplate.get(index);
        } else {
            RedisDBIndex dbIndex = RedisDBIndex.getEnum(index);
            RedisConnectionFactory redisConnectionFactory =
                    QczRedisConnectionFactory.createLettuceConnectionFactory(dbIndex.getValue(), redisProperties);

            template = new QczRedisTemplate(dbIndex, redisConnectionFactory);

            mapQczRedisTemplate.put(index, template);
        }

        return template;
    }

    /**
     * 截取的字符串末尾数字转index
     * @param strIndex
     * @return      返回转换后的数字，异常时返回0
     */
    private int toInt(String strIndex) {
        int index = RedisDBIndex.DB_INDEX_0.getValue();

        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNumeric = pattern.matcher(strIndex);
        if( isNumeric.matches() )
            index = Integer.parseInt(strIndex);

        return index;
    }
}
