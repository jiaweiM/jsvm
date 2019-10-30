package jsvm;

import java.util.Objects;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 29 Oct 2019, 8:49 PM
 */
public class Pair<K, V>
{
    /**
     * Create a pair.
     *
     * @param key   key value
     * @param value value
     * @param <K>   type of the key
     * @param <V>   type of the value
     */
    public static <K, V> Pair<K, V> create(K key, V value)
    {
        return new Pair<>(key, value);
    }

    private K key;
    private V value;

    public Pair(K key, V value)
    {
        this.key = key;
        this.value = value;
    }

    public K getKey()
    {
        return key;
    }

    public V getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return key.equals(pair.key) &&
                value.equals(pair.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(key, value);
    }
}
