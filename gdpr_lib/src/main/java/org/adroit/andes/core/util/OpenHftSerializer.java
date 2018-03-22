package org.adroit.andes.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.annotation.NotNull;
import net.openhft.chronicle.core.util.ReadResolvable;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

@SuppressWarnings("unchecked")
public final class OpenHftSerializer<T> implements BytesReader<T>, BytesWriter<T>, ReadResolvable<OpenHftSerializer<T>>{

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenHftSerializer.class);
	private final Serializer<T> SERIALIZER;

    public OpenHftSerializer() {
    	this.SERIALIZER = new Serializer<T>((Class<T>) AndesUtil.getTypeArguments(OpenHftSerializer.class, this.getClass()).get(0));
	}

	@Override
	public void write(Bytes out, T toWrite) {
		try {
			byte[] serizliedBytes = this.SERIALIZER.serialize(toWrite);
			out.writeInt(serizliedBytes.length);
			out.write(serizliedBytes, 0, serizliedBytes.length);
		} catch (Exception e) {
			LOGGER.error("Error while persisting data.", e);
		}
	}

	@Override
	public T read(Bytes in, T using) {
		try {
			int serializedBytesLength = in.readInt();
			byte[] serizliedBytes = new byte[serializedBytesLength];
			in.read(serizliedBytes, 0, serializedBytesLength);
			using = this.SERIALIZER.deserialize(serizliedBytes);
		} catch (Exception e) {
			LOGGER.error("Error while reading data.", e);
		}
		return using;
	}

    public void writeMarshallable(@NotNull WireOut wireOut) {
        // no fields to write
    }

    public void readMarshallable(@NotNull WireIn wireIn) {
        // no fields to read
    }

	@Override
	public OpenHftSerializer<T> readResolve() {
		return this;
	}
}
