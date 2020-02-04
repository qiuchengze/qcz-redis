package qcz.zone.redis.serialized;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.util.CharsetUtil;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

import java.nio.charset.Charset;

/**
 * @author: qiuchengze
 * @create: 2019 - 09 - 28
 */
public class FastJsonSerializable<T> implements RedisSerializer<T> {
    private Charset charset = CharsetUtil.UTF_8;
    private Class<T> clazz = null;

    public FastJsonSerializable(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(@Nullable T t) throws SerializationException {
        if (null == t) {
            return new byte[0];
        }

        String str = JSON.toJSONString(t, SerializerFeature.WriteClassName);

        return str.getBytes(charset);
    }

    @Override
    public T deserialize(@Nullable byte[] bytes) throws SerializationException {
        if (null == bytes || bytes.length <= 0)
            return null;

        String str = new String(bytes, charset);

        return (T) JSON.parseObject(str, clazz);
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }
}
