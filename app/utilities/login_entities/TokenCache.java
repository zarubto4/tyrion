package utilities.login_entities;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;
import utilities.web_socket.message_objects.WS_Token;

import java.util.ArrayList;

public class TokenCache {

    private long timeToLive;
    private final LRUMap crunchifyCacheMap;

    protected class CrunchifyCacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public WS_Token value;

        protected CrunchifyCacheObject(WS_Token value) {
            this.value = value;
        }
    }

    public TokenCache(long crunchifyTimeToLive, final long crunchifyTimerInterval, int maxItems) {
        this.timeToLive = crunchifyTimeToLive * 1000;

        crunchifyCacheMap = new LRUMap(maxItems);

        if (timeToLive > 0 && crunchifyTimerInterval > 0) {

            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(crunchifyTimerInterval * 1000);
                        } catch (InterruptedException ex) {
                        }
                        cleanup();
                    }
                }
            });

            t.setDaemon(true);
            t.start();
        }
    }

    public void put(String key, WS_Token value) {
        synchronized (crunchifyCacheMap) {
            crunchifyCacheMap.put(key, new CrunchifyCacheObject(value));
        }
    }

    @SuppressWarnings("unchecked")
    public WS_Token get(String key) {
        synchronized (crunchifyCacheMap) {
            CrunchifyCacheObject c = (CrunchifyCacheObject) crunchifyCacheMap.get(key);

            if (c == null)
                return null;
            else {
                c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }

    public void remove(String key) {
        synchronized (crunchifyCacheMap) {
            crunchifyCacheMap.remove(key);
        }
    }

    public int size() {
        synchronized (crunchifyCacheMap) {
            return crunchifyCacheMap.size();
        }
    }

    @SuppressWarnings("unchecked")
    public void cleanup() {

        long now = System.currentTimeMillis();
        ArrayList<String> deleteKey = null;

        synchronized (crunchifyCacheMap) {
            MapIterator itr = crunchifyCacheMap.mapIterator();

            deleteKey = new ArrayList<>((crunchifyCacheMap.size() / 2) + 1);
            String key = null;
            CrunchifyCacheObject c = null;

            while (itr.hasNext()) {
                key = (String) itr.next();
                c = (CrunchifyCacheObject) itr.getValue();

                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }

        for (String key : deleteKey) {
            synchronized (crunchifyCacheMap) {
                crunchifyCacheMap.remove(key);
            }

            Thread.yield();
        }
    }
}