package qcz.zone.redis.serialized;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

import java.io.*;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 30
 */
public class ObjectStreamSerializable<T> implements RedisSerializer<T> {

    @Override
    public byte[] serialize(@Nullable T t) throws SerializationException {
        if (null == t)
            return new byte[0];;

        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;

        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(t);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (null != oos)
                    oos.close();

                if (null != bos)
                    bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    @Override
    public T deserialize(@Nullable byte[] bytes) throws SerializationException {
        if (null == bytes || bytes.length <= 0)
            return null;

        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;

        try {
            bis = new ByteArrayInputStream (bytes);
            ois = new ObjectInputStream (bis);

            return (T) ois.readObject();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (null != ois)
                    ois.close();

                if (null != bis)
                    bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
