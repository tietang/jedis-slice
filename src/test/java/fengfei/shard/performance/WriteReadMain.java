package fengfei.shard.performance;

import fengfei.shard.Ploy;
import fengfei.shard.Selector;
import fengfei.shard.impl.LoopPloy;
import fengfei.shard.redis.JedisShards;
import fengfei.shard.redis.RedisCommand;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

public class WriteReadMain implements Runnable {


    // for test
    private static long sleepTime = 0;
    private static int startWriteNum = 0;
    private static int threads = 5;
    private static String hostStr = "172.17.20.32";
    private static int port = 10000;

    private static String hosts[];

    public static void main(String args[]) throws Exception {
        Count.start(1);
        Count.setMaxRowNums(1000);
        // 0=sleep time,1=thread num

        if (args.length >= 1) {
            sleepTime = Long.parseLong(args[0]);
        }
        if (args.length >= 2) {
            threads = Integer.parseInt(args[1]);
        }
        if (args.length >= 3) {
            hostStr = args[2];

        }
        if (args.length >= 4) {
            port = Integer.parseInt(args[3]);
        }
        hosts = hostStr.split(",");
        System.out.println("");
        System.out.println("request sleep time(ms): " + sleepTime);
        System.out.println("request thread num: " + threads);
        System.out.println("hosts: " + hostStr);
        System.out.println("port: " + port);
        System.out.println("");
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(new WriteReadMain(), "WriteReadTest-" + i);
        }
        for (int j = 0; j < ts.length; j++) {
            ts[j].start();
            System.out.println("start thread:" + ts[j].getName());
        }
    }

    @Override
    public void run() {
        Count.setStartWriteNum(startWriteNum);
        Config cfg = new Config();
        Ploy ploy = new LoopPloy();
        JedisShards redis = new JedisShards("127.0.0.1:6379,127.0.0.1:6379", 3000, ploy, cfg);

        //
        RedisCommand read = redis.create(RedisCommand.class);
        RedisCommand write = redis.create(RedisCommand.class, Selector.Write);
        try {
            final WriteReadService writeRead = new WriteReadService(write, read);

            while (true) {

                try {

                    writeRead.read();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
