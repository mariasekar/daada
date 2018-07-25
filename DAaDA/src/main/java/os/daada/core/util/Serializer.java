package os.daada.core.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import os.daada.core.AndesException;

@SuppressWarnings("serial")
public class Serializer<T> implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Serializer.class);
	private final KryoPool kryoPool = new KryoPoolImpl();
	private Class<T> persistentClass;

	public interface KryoPool {
		Kryo get();
		void yield(Kryo kryo);
	}

	public static final class KryoPoolImpl implements KryoPool {

		private final Queue<Kryo> objects = new ConcurrentLinkedQueue<Kryo>();

		public KryoPoolImpl() {
		}

		public Kryo get() {
			Kryo kryo;
			if ((kryo = objects.poll()) == null) {
				kryo = createInstance();
			}
			return kryo;
		}

		public void yield(Kryo kryo) {
			objects.offer(kryo);
		}

		/**
		 * Sub classes can customize the Kryo instance by overriding this method
		 *
		 * @return create Kryo instance
		 */
		private Kryo createInstance() {
			Kryo kryo = new Kryo();
			kryo.setReferences(false);
			return kryo;
		}

	}

    public Serializer(Class<T> persistentClass) {
    	this.persistentClass = persistentClass;
	}

    public final byte[] serialize(T object) throws AndesException {
		byte[] serializedBytes = null;
		Kryo kryo = null;
		try {
			kryo = kryoPool.get();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(150);
	    	Output output = new Output(byteArrayOutputStream);
	    	kryo.writeObject(output, object);
	        serializedBytes = output.toBytes();
		} catch(Exception e) {
			LOGGER.error("Error while serializing the object.", e);
			e.printStackTrace();
		} finally {
            if (kryo != null) {
                this.kryoPool.yield(kryo);
            }
        }
		return serializedBytes;
    }

    public T deserialize(InputStream inputStream) throws AndesException {
		Kryo kryo = null;
		try {
			if(inputStream != null && this.persistentClass != null) {
				kryo = kryoPool.get();
				return (T) kryo.readObject(new Input(inputStream), this.persistentClass);
			}
		} catch(Exception e) {
			LOGGER.error("Error while deserializing the bytes.", e);
			throw new AndesException("Error while serializing the object.", e);
		} finally {
            if (kryo != null) {
                this.kryoPool.yield(kryo);
            }
        }
		return null;
    }
	
	public final T deserialize(byte[] bytes) throws AndesException {
		Kryo kryo = null;
		try {
			if(bytes != null && bytes.length > 0 && this.persistentClass != null) {
				kryo = kryoPool.get();
				return (T) kryo.readObject(new Input(bytes), this.persistentClass);
			}
		} catch(Exception e) {
			LOGGER.error("Error while deserializing the bytes.", e);
			throw new AndesException("Error while serializing the object.", e);
		} finally {
            if (kryo != null) {
                this.kryoPool.yield(kryo);
            }
        }
		return null;
    }

	public void serialize(T value, ByteBuffer buf) {
		byte[] bytes = null;
		try {
			bytes = this.serialize(value);
	        buf.put(bytes);
		} catch (AndesException e) {
			LOGGER.error("Error while serializing the object.", e);
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.error("Error while serializing the object.", e);
			e.printStackTrace();
		}
	}

	public T deserialize(ByteBuffer buf) {
		T deserializedObj = null;
        try {
        	byte[] bytes = new byte[buf.capacity()];
        	buf.get(bytes);
        	deserializedObj = this.deserialize(bytes);
		} catch (AndesException e) {
			LOGGER.error("Error while de-serializing the byte to object.", e);
		}
        return deserializedObj;
	}

	public int serializedSize(T value) {
		int objectSize = 0;
		try {
			objectSize = this.serialize(value).length;
		} catch (AndesException e) {
			LOGGER.error("Error while calculating the serialized object size.", e);
		}
		return objectSize;
	}
}
