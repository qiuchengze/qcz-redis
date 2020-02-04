package qcz.zone.redis.constant;

/**
 * @author: qiuchengze
 * @create: 2019 - 11 - 01
 */
public enum RedisDBIndex {
    DB_INDEX_NULL(-1),
    DB_INDEX_0(0),
    DB_INDEX_1(1),
    DB_INDEX_2(2),
    DB_INDEX_3(3),
    DB_INDEX_4(4),
    DB_INDEX_5(5),
    DB_INDEX_6(6),
    DB_INDEX_7(7),
    DB_INDEX_8(8),
    DB_INDEX_9(9),
    DB_INDEX_10(10),
    DB_INDEX_11(11),
    DB_INDEX_12(12),
    DB_INDEX_13(13),
    DB_INDEX_14(14),
    DB_INDEX_15(15);

    private int value;
    private RedisDBIndex(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RedisDBIndex getEnum(int index) {
        for (RedisDBIndex e : RedisDBIndex.values()) {
            if (e.getValue() == index) return e;
        }

        return null;
    }
}
