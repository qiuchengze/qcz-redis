package qcz.zone.redis.template;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;
import qcz.zone.redis.constant.RedisDBIndex;
import qcz.zone.redis.serialized.FastJsonSerializable;
import qcz.zone.redis.serialized.ObjectStreamSerializable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: qiuchengze
 * @create: 2019 - 10 - 04
 */
public class QczRedisTemplate<K, V> extends RedisTemplate<K, V> implements Cloneable, Serializable {
    private RedisDBIndex dbIndex = RedisDBIndex.DB_INDEX_0;
    private RedisSerializer valRedisSerializer = null;

    public QczRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        this(RedisDBIndex.DB_INDEX_0, redisConnectionFactory);
    }

    public QczRedisTemplate(RedisDBIndex dbIndex, RedisConnectionFactory redisConnectionFactory) {
        if (null != dbIndex)
            this.dbIndex = dbIndex;

        if (null == redisConnectionFactory)
            throw new RuntimeException("redisConnectionFactory is null");
        setConnectionFactory(redisConnectionFactory);

        // 默认使用fastjson序列化
        if (null == valRedisSerializer)
            valRedisSerializer = new FastJsonSerializable(Object.class);

        // 配置序列化器
        // value值的序列化采用fastJsonRedisSerializer
        setValueSerializer(valRedisSerializer);
        setHashValueSerializer(valRedisSerializer);

        // 字符串序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key的序列化采用StringRedisSerializer
        setKeySerializer(stringRedisSerializer);
        setHashKeySerializer(stringRedisSerializer);
        afterPropertiesSet();
    }

    public int getIndex() {
        return dbIndex.getValue();
    }

    public void setDbIndex(RedisDBIndex dbIndex) {
        this.dbIndex = dbIndex;

        LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) getConnectionFactory();
        lettuceConnectionFactory.setDatabase(dbIndex.getValue());
    }

    public RedisSerializer getRedisSerializer() {
        return valRedisSerializer;
    }

    public void setRedisSerializer(RedisSerializer redisSerializer) {
        this.valRedisSerializer = redisSerializer;

        setValueSerializer(valRedisSerializer);
        setHashValueSerializer(valRedisSerializer);
    }

    public void selectFastJsonSerializable() {
        if (this.valRedisSerializer instanceof FastJsonSerializable)
            return;

        setRedisSerializer(new FastJsonSerializable(Object.class));
    }

    public void selectObjectStreamSerializable() {
        if (this.valRedisSerializer instanceof ObjectStreamSerializable)
            return;

        setRedisSerializer(new ObjectStreamSerializable());
    }

    //    @Override
//    public QczRedisTemplate clone() {
//        QczRedisTemplate redisTemplate = null;
//
//        try {
//            redisTemplate = (QczRedisTemplate) super.clone();
//        } catch (CloneNotSupportedException e) {
//            e.printStackTrace();
//        } finally {
//            return redisTemplate;
//        }
//    }
//
//    public QczRedisTemplate deepClone() {
//        QczRedisTemplate redisTemplate = null;
//        ByteArrayOutputStream bos = null;
//        ObjectOutputStream oos = null;
//        ByteArrayInputStream bis = null;
//        ObjectInputStream ois = null;
//
//        try {
//            /* 写入当前对象的二进制流 */
//            bos = new ByteArrayOutputStream();
//            oos = new ObjectOutputStream(bos);
//            oos.writeObject(this);
//            oos.flush();
//
//            /* 读出二进制流产生的新对象 */
//            bis = new ByteArrayInputStream(bos.toByteArray());
//            ois = new ObjectInputStream(bis);
//            redisTemplate = (QczRedisTemplate) ois.readObject();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (null != bos)
//                    bos.close();
//                if (null != oos)
//                    oos.close();
//                if (null != bis)
//                    bis.close();
//                if (null != ois)
//                    ois.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                return redisTemplate;
//            }
//        }
//    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public Boolean expire(K key, long time) {
        try {
            if (time > 0) {
                expire(key, time, TimeUnit.SECONDS);

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public Long getExpire(K key) {
        return getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
//    public Boolean hasKey(String key) {
//        try {
//            return hasKey(key);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return false;
//    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(K... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                delete(key[0]);
            } else {
                delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public V get(K key) {
        return key == null ? null : opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public Boolean set(K key, V value) {
        try {
            opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public Boolean set(K key, V value, long time) {
        try {
            if (time > 0) {
                opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间 time要大于0 如果time小于等于0 将设置无限期
     * @param timeUnit  时间单位
     * @return true成功 false 失败
     */
    public Boolean set(K key, V value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public Long incr(K key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public Long decr(K key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return opsForValue().increment(key, -delta);
    }

//    /**
//     * 返回匹配的所有key集合(可以使用"*"通配符，获取所有key)
//     * @param pattern   通配字符串
//     * @return
//     */
//    public Set<Object> keys(String pattern) {
//        return keys(pattern);
//    }

    /**
     * 返回匹配的所有value集合（根据key关键字或通配符）
     * @param parttern
     * @return
     */
    public List<V> vals(K parttern) {
        Set<K> keys = keys(parttern);
        if (CollectionUtils.isEmpty(keys))
            return null;

        return vals(keys);
    }

    /**
     * 返回匹配的所有value集合（根据key集合）
     * @param keys
     * @return
     */
    public List<V> vals(Set<K> keys) {
        if (CollectionUtils.isEmpty(keys))
            return null;

        return opsForValue().multiGet(keys);
    }

    /**
     * 清空当前db
     */
    public void flushDb() {
        RedisConnection redisConnection = getConnectionFactory().getConnection();

        if (null != redisConnection)
            redisConnection.flushDb();
    }

    /**
     * 获取当前db的size
     * @return
     */
    public Long dbSize() {
        RedisConnection redisConnection = getConnectionFactory().getConnection();

        if (null != redisConnection)
            return redisConnection.dbSize();

        return 0L;
    }

    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public V hget(K key, Object item) {
        return (V) opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<K, V> hmget(K key) {
        return (Map<K, V>) opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public Boolean hmset(K key, Map<K, V> map) {
        try {
            opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public Boolean hmset(K key, Map<K, V> map, long time) {
        try {
            opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public Boolean hset(K key, K item, V value) {
        try {
            opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public Boolean hset(K key, K item, V value, long time) {
        try {
            opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     * @param key   键 不能为null
     * @param item  项 可以使多个 不能为null
     */
    public void hdel(K key, Object... item) {
        opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public Boolean hHasKey(K key, Object item) {
        return opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public Double hincr(K key, K item, double by) {
        return opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    public Double hdecr(K key, K item, double by) {
        return opsForHash().increment(key, item, -by);
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<V> sGet(K key) {
        try {
            return opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public Boolean sHasKey(K key, Object value) {
        try {
            return opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public Long sSet(K key, V... values) {
        try {
            return opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public Long sSetAndTime(K key, long time, V... values) {
        try {
            Long count = opsForSet().add(key, values);
            if (time > 0)
                expire(key, time);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public Long sGetSetSize(K key) {
        try {
            return opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public Long setRemove(K key, Object... values) {
        try {
            Long count = opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List<V> lGet(K key, long start, long end) {
        try {
            return opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public Long lGetListSize(K key) {
        try {
            return opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public V lGetIndex(K key, long index) {
        try {
            return opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public Boolean lSet(K key, V value) {
        try {
            opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public Boolean lSet(K key, V value, long time) {
        try {
            opsForList().rightPush(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public Boolean lSet(K key, List<V> value) {
        try {
            opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public Boolean lSet(K key, List<V> value, long time) {
        try {
            opsForList().rightPushAll(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public Boolean lUpdateIndex(K key, long index, V value) {
        try {
            opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public Long lRemove(K key, long count, Object value) {
        try {
            Long remove = opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
