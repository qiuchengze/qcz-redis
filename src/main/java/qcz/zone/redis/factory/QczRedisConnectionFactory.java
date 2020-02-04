package qcz.zone.redis.factory;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.StringUtils;

/**
 * @author: qiuchengze
 * @create: 2019 - 11 - 02
 */

public class QczRedisConnectionFactory {

    public static RedisConnectionFactory createLettuceConnectionFactory(int dbIndex, RedisProperties redisProperties) {
        /* ========= 基本配置 ========= */
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisProperties.getHost());
        redisStandaloneConfiguration.setPort(redisProperties.getPort());
        redisStandaloneConfiguration.setDatabase(dbIndex);
        if (!StringUtils.isEmpty(redisProperties.getPassword())) {
            RedisPassword redisPassword = RedisPassword.of(redisProperties.getPassword());
            redisStandaloneConfiguration.setPassword(redisPassword);
        }

        GenericObjectPoolConfig genericObjectPoolConfig = null;
        RedisProperties.Lettuce lettuce = redisProperties.getLettuce();
        if (null != lettuce) {
            RedisProperties.Pool pool = lettuce.getPool();
            if (null != pool) {
                /* ========= 连接池通用配置 ========= */
                genericObjectPoolConfig = new GenericObjectPoolConfig();
                genericObjectPoolConfig.setMaxTotal(pool.getMaxActive());
                genericObjectPoolConfig.setMinIdle(pool.getMinIdle());
                genericObjectPoolConfig.setMaxIdle(pool.getMaxIdle());
                genericObjectPoolConfig.setMaxWaitMillis(pool.getMaxWait().toMillis());
                if (null != pool.getTimeBetweenEvictionRuns())
                    genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(pool.getTimeBetweenEvictionRuns().toMillis());
            }
        }

        /* ========= lettuce pool ========= */
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder =
                LettucePoolingClientConfiguration.builder();

        if (null != genericObjectPoolConfig)
            builder.poolConfig(genericObjectPoolConfig);    // 如果只需要单一连接，不需要使用连接池可以不设置此项

        if (null != redisProperties.getTimeout())
            builder.commandTimeout(redisProperties.getTimeout());

        LettuceConnectionFactory lettuceConnectionFactory =
                new LettuceConnectionFactory(redisStandaloneConfiguration, builder.build());
        // ShareNativeConnection参数：默认为 true，意思是共用这一个连接，所以默认情况下 lettuce 的连接池是没有用的；
        // 如果需要使用连接池，设置为 false
        lettuceConnectionFactory.setShareNativeConnection(false);
        lettuceConnectionFactory.afterPropertiesSet();

        /* ========= jedis pool ========= */
        /*
        JedisClientConfiguration.DefaultJedisClientConfigurationBuilder builder = (JedisClientConfiguration.DefaultJedisClientConfigurationBuilder) JedisClientConfiguration
                .builder();
        builder.connectTimeout(Duration.ofSeconds(timeout));
        builder.usePooling();
        builder.poolConfig(genericObjectPoolConfig);
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(configuration, builder.build());
        // 连接池初始化
        connectionFactory.afterPropertiesSet();
        */

        return lettuceConnectionFactory;
    }
}
