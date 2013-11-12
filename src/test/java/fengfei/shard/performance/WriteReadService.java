package fengfei.shard.performance;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.relation.RelationException;

import fengfei.shard.redis.RedisCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteReadService {

    static Logger logger = LoggerFactory.getLogger("CLient");
    protected static Random rand = new Random(System.currentTimeMillis());
    protected long totalSetupTime;
    public AtomicInteger ct = new AtomicInteger(1);

    int port = 10000;
    public AtomicInteger sidGenerator = new AtomicInteger(1);
    RedisCommand write;
    RedisCommand read;

    public WriteReadService(RedisCommand write, RedisCommand read) {
        super();
        this.write = write;
        this.read = read;
    }

    public static int random(int minNum, int maxNum) {
        int r = random.nextInt() % maxNum;
        if (r > maxNum || r < minNum) {
            r = random(minNum, maxNum);
        }
        return Math.abs(r);
    }

    protected static Random random = new Random();

    public void write() throws RelationException {

        int countNum = ct.getAndIncrement();

        // System.out.println(result.getResult().size());
        Count.incrementWriteNum();
        Count.increment();
    }

    public void read() throws RelationException {

        int countNum = ct.getAndIncrement();

        // System.out.println(result.getResult().size());
        Count.incrementReadNum();
        Count.increment();
    }

}
