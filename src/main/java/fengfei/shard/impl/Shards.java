package fengfei.shard.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fengfei.shard.AutohealthCheckThread;
import fengfei.shard.InstanceInfo;
import fengfei.shard.Ploy;
import fengfei.shard.Pools;
import fengfei.shard.Selector;
import fengfei.shard.Shard;

/**
 * <pre>
 * example 1:
 * 		PoolableSharddRedis redis = new PoolableSharddRedis(
 * 				"192.168.1.3:6379,192.168.1.4:6379,192.168.1.5:6379 192.168.1.6:6379,192.168.1.7:6379,192.168.1.8:6379", 60000, new Hashselector(),
 * 				config);
 * 		RedisComand rc = redis.createRedisCommand();
 * 	    rc.set("key", "value");
 * 		redis.close();
 * 
 * * example 2: 
 * 		Selector selector = new Hashselector();
 * 		selector.setTimeout(60);
 * 		selector.setPoolConfig(config);
 * 		selector.setploy(new Loopploy());
 * 		//Shard 0: master:192.168.1.3:6379 slave:192.168.1.4:6379 192.168.1.5:6379
 * 		selector.addShard(0, "192.168.1.3:6379", "192.168.1.4:6379",
 * 				"192.168.1.5:6379");
 * 		//Shard 1: master:192.168.1.6:6379 slave:192.168.1.7:6379 192.168.1.8:6379
 * 		selector.addShard(0, "192.168.1.6:6379", "192.168.1.7:6379",
 * 				"192.168.1.8:6379");
 * 
 * 		PoolableSharddRedis redis = new PoolableSharddRedis(selector);
 * 		RedisComand rc = redis.createRedisCommand();
 * </pre>
 * 
 * @author
 * 
 */
public class Shards<T> {

    public final static int ReadWrite = 0;
    public final static int ReadOnly = 2;
    public final static int WriteOnly = 1;

    private static Logger logger = LoggerFactory.getLogger(Shards.class);
    protected Pools<T> pools;
    protected Selector selector = new HashSelector();
    protected AtomicInteger lastId = new AtomicInteger(0);
    protected boolean isPoolable = false;
    protected GenericObjectPool.Config config = DefaultPools.DefaultConfig;
    protected PoolableObjectFactoryCreator<T> factoryCreator;

    public Shards(
        Selector selector,
        PoolableObjectFactoryCreator<T> factoryCreator,
        boolean isPoolable) {
        this.selector = selector;
        this.isPoolable = isPoolable;
        this.factoryCreator = factoryCreator;
        createPool(selector, config);
        startFailOver();
    }

    public Shards(
        Selector selector,
        PoolableObjectFactoryCreator<T> factoryCreator,
        GenericObjectPool.Config config) {
        this.selector = selector;
        this.isPoolable = true;
        this.factoryCreator = factoryCreator;
        this.config = config;
        createPool(selector, config);
        startFailOver();
    }

    /**
     * <pre>
     * 
     *  hosts: MasterHost:port[,SlaveHost:port...] MasterHost:port[,SlaveHost:port...] ...
     * </pre>
     * 
     * @param hosts
     * @param timeout
     * @param selector
     * @param isPoolable
     */
    public Shards(
        String hosts,
        int timeout,
        Selector selector,
        PoolableObjectFactoryCreator<T> factoryCreator,
        boolean isPoolable) {
        super();
        this.isPoolable = isPoolable;
        this.factoryCreator = factoryCreator;
        this.selector = selector;
        init(hosts, timeout, selector.getPloy(), null);
    }

    public Shards(
        String hosts,
        int timeout,
        Selector selector,
        PoolableObjectFactoryCreator<T> factoryCreator,
        GenericObjectPool.Config config) {
        super();
        this.isPoolable = true;
        this.config = config;
        this.factoryCreator = factoryCreator;
        this.selector = selector;
        init(hosts, timeout, selector.getPloy(), config);
    }

    public Shards(
        String hosts,
        int timeout,
        Ploy ploy,
        PoolableObjectFactoryCreator<T> factoryCreator,
        boolean isPoolable) {
        super();
        this.isPoolable = isPoolable;
        this.factoryCreator = factoryCreator;
        init(hosts, timeout, ploy, null);
    }

    public Shards(
        String hosts,
        int timeout,
        Ploy ploy,
        PoolableObjectFactoryCreator<T> factoryCreator,
        GenericObjectPool.Config config) {
        super();
        this.isPoolable = true;
        this.config = config;
        this.factoryCreator = factoryCreator;
        init(hosts, timeout, ploy, config);
    }

    protected void init(String hosts, int timeout, Ploy ploy, GenericObjectPool.Config config) {
        pools = new DefaultPools<T>(config, getPoolableObjectFactoryCreator());
        Selectors.initSelector(selector, hosts, timeout, ploy);
        createPool(selector, config);
        startFailOver();
    }

    public PoolableObjectFactoryCreator<T> getPoolableObjectFactoryCreator() {
        return factoryCreator;
    }

    protected AutohealthCheckThread<T> failOver;

    private void startFailOver() {
        failOver = new AutohealthCheckThread<>(selector, pools);
        failOver.start();
    }

    protected void createPool(Shard shard, GenericObjectPool.Config config) {
        if (isPoolable) {
            if (pools == null) {
                pools = new DefaultPools<T>(config, getPoolableObjectFactoryCreator());
            }
            pools.createPool(shard);
        }
    }

    protected void createPool(Selector selector, GenericObjectPool.Config config) {
        if (isPoolable) {
            if (pools == null) {
                pools = new DefaultPools<T>(config, getPoolableObjectFactoryCreator());
            }
            pools.createPool(selector);
        }
    }

    /**
     * <pre>
     * eg: 
     *  	masterHost : 192.168.1.22:6300
     *  	slaveHosts : 192.168.1.22:6301,192.168.1.22:6302,192.168.1.22:6303
     * 	addHosts("192.168.1.22:6300", "192.168.1.22:6301", "192.168.1.22:6302", "192.168.1.22:6303")
     * </pre>
     * 
     * @param masterHost
     * @param slaveHosts
     */
    public void addHosts(int timeout, String masterHost, String... slaveHosts) {
        int id = lastId.get();
        Shard shard = Shard.createShard(id, timeout, masterHost, slaveHosts);
        selector.addShard(shard);
        lastId.incrementAndGet();
    }

    public <I> I create(Class<I> iface) {
        return create(iface, ReadWrite);
    }

    public <I> I create(Class<I> iface, int rw) {

        InvocationHandler handler = null;
        if (isPoolable) {
            handler = new PoolableHandler<I>(iface, rw);
            logger.debug("create RedisComand with pool.");
        } else {
            handler = new Handler<I>(iface, rw);
            logger.debug("create RedisComand without pool.");
        }
        return (I) Proxy.newProxyInstance(iface.getClassLoader(), new Class[] { iface }, handler);
    }

    public void close() {
        if (isPoolable)
            pools.closeAll();
        failOver.exit();
    }

    Random random = new Random(19800202);

    private class PoolableHandler<I> implements InvocationHandler {

        Class<I> iface;
        int readWrite = ReadWrite;

        public PoolableHandler(Class<I> iface, int readWrite) {
            super();
            this.readWrite = readWrite;
            this.iface = iface;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            T call = null;
            InstanceInfo info = null;
            String key = null;
            try {

                Class<?> argsClass[] = method.getParameterTypes();
                if (args != null && args.length > 0) {
                    // argsClass = new Class<?>[args.length];
                    // for (int i = 0; i < args.length; i++) {
                    // argsClass[i] = args[i].getClass();
                    // System.out.println(argsClass[i].isPrimitive());
                    // }

                    Object obj = args[0];
                    if (obj instanceof byte[]) {
                        key = String.valueOf((byte[]) obj);
                    } else {
                        key = obj.toString();
                    }
                } else {
                    // argsClass = new Class<?>[] {};
                    key = String.valueOf(random.nextInt());
                }
                info = selector.select(new String(key), readWrite);
                if(!info.isAvailable()){
                	 throw new Exception("The Server is unavailable for key:" + key);
                }
                call = pools.borrow(info);
                if (call == null) {
                    throw new Exception("can't connected server for key:" + key);
                }

                Method origin = call.getClass().getMethod(method.getName(), argsClass);
                Object obj = origin.invoke(call, args);
                return obj;
            } catch (Throwable e) {
                logger.error("Can not operate server for key:" + key, e);
                throw e;

            } finally {
                pools.returnPool(info, call);
            }
        }
    }

    private class Handler<I> implements InvocationHandler {

        Class<I> iface;
        int readWrite = ReadWrite;

        public Handler(Class<I> iface, int readWrite) {
            super();
            this.readWrite = readWrite;
            this.iface = iface;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            T call = null;
            InstanceInfo info = null;
            PoolableObjectFactory<T> pof = null;
            try {
                byte[] key = null;

                Class<?> argsClass[] = method.getParameterTypes();
                if (args != null && args.length > 0) {
                    // argsClass = new Class<?>[args.length];
                    // for (int i = 0; i < args.length; i++) {
                    // argsClass[i] = args[i].getClass();
                    // System.out.println(argsClass[i].isPrimitive());
                    // }

                    Object obj = args[0];
                    if (obj instanceof byte[]) {
                        key = (byte[]) obj;
                    } else {
                        key = obj.toString().getBytes();
                    }
                } else {
                    // argsClass = new Class<?>[] {};
                    key = String.valueOf(random.nextLong()).getBytes();
                }
                info = selector.select(new String(key), readWrite);
                pof = getPoolableObjectFactoryCreator().create(info);

                call = pof.makeObject();

                if (call == null) {
                    throw new Exception("Can't connected redis");
                }
                if (!pof.validateObject(call)) {
                    throw new Exception("Server can't be connected.");
                }
                Method origin = call.getClass().getMethod(method.getName(), argsClass);
                Object obj = origin.invoke(call, args);
                return obj;
            } catch (Throwable e) {
                logger.error("Can not operate Server ", e);
                throw e;

            } finally {
                try {

                    pof.destroyObject(call);

                } catch (Exception e) {
                    logger.error("close connection error ", e);
                }
            }
        }
    }

    public static interface RetryCallback {

        void execute() throws Exception;
    }

}
