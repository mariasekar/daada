package org.adroit.andes.core;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import org.adroit.andes.core.crypt.ICrypto;

import com.typesafe.config.Config;

import net.openhft.chronicle.map.ChronicleMap;

public abstract class AbstractAndes implements IAndes {

	static {
		try {
			String cacheDirPath = "andescache";
			File baseDir = new File(System.getProperty("java.io.tmpdir"));

			if(cacheDirPath.startsWith("/")) {
				cacheDirPath = cacheDirPath.substring(1);
			}
			File cacheDir = new File(baseDir, cacheDirPath);
	        if(!cacheDir.exists()) {
	            cacheDir.mkdir();
	        }
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));
	protected final ICrypto cyptoHandler;
	protected final Config config;
	protected IPiiDataValidator piiDataValidator;

	public AbstractAndes(final Config config, final ICrypto cyptoHandler) throws AndesException {
		this.config = config;
		this.cyptoHandler = cyptoHandler;
	}

	protected static final ConcurrentMap<String, String> createMap(String cacheName) {
		try{

			final ConcurrentMap<String, String> CACHE_MAP = ChronicleMap
				    .of(String.class, String.class)
				    .name(cacheName)
				    .averageKeySize(50)
				    .averageValueSize(175)
				    .entries(5000000)
				    /*.keyMarshaller(new OpenHftSerializer<>())
				    .valueMarshaller(new OpenHftSerializer<>())*/
				    .maxBloatFactor(10)
				    .actualSegments(16)
				    .createOrRecoverPersistedTo(new File(TEMP_DIR, "andescache/" + cacheName + ".db"));
			return CACHE_MAP;
		} catch(IOException e) {
			System.out.println(e);
		}
		return null;
	}
}

