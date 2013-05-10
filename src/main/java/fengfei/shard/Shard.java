package fengfei.shard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Shard {

    public final static int StatusNormal = 1;
    public final static int StatusError = 0;
    protected int id;
    protected InstanceInfo master;
    protected List<InstanceInfo> slaves;
    //
    protected List<InstanceInfo> canceledSlaves = new ArrayList<>();
    protected InstanceInfo canceledMaster;
    protected List<InstanceInfo> configedInfos = new ArrayList<>();
    protected int status = StatusNormal;

    public Shard(InstanceInfo master, List<InstanceInfo> slaves) {
        super();

        this.master = master;
        this.slaves = slaves;

        if (slaves == null) {
            slaves = new ArrayList<>();
        }
        configedInfos.add(master);
        configedInfos.addAll(slaves);
    }

    public Shard(int id, InstanceInfo master, List<InstanceInfo> slaves) {
        super();
        this.id = id;
        this.master = master;
        this.slaves = slaves;

        if (slaves == null) {
            slaves = new ArrayList<>();
        }
        configedInfos.add(master);
        configedInfos.addAll(slaves);
    }

    public static Shard createShard(int timeout, String masterHost, String... slaveHosts) {
        String mhp[] = masterHost.split(":");
        InstanceInfo master = new InstanceInfo(mhp[0], Integer.parseInt(mhp[1]), timeout);
        List<InstanceInfo> slaves = new ArrayList<>();
        if (slaveHosts != null && slaveHosts.length > 0) {

            for (String shost : slaveHosts) {
                String shp[] = shost.split(":");
                InstanceInfo slave = new InstanceInfo(shp[0], Integer.parseInt(shp[1]), timeout);
                slave.setMaster(false);
                slaves.add(slave);
            }

        }
        return new Shard(master, slaves);

    }

    public static Shard createShard(int id, int timeout, String masterHost, String... slaveHosts) {
        Shard shard = createShard(timeout, masterHost, slaveHosts);
        shard.setId(id);
        return shard;

    }

    public int getStatus() {
        return status;
    }

    public void addInstanceInfo(InstanceInfo info) {
        boolean isMaster = info.isMaster();
        if (isMaster) {
            if (master == null && canceledMaster == null) {
                master = info;
            } else {
                throw new ShardException("master is seted.");
            }
        } else {
            slaves.add(info);
        }
        configedInfos.add(info);
    }

    Lock lock = new ReentrantLock();

    public void cancel(InstanceInfo info) {
        boolean isMaster = info.isMaster();
        lock.lock();
        try {
            if (isMaster && info.equals(master)) {
                canceledMaster = master;
                master = null;
            } else {
                canceledSlaves.add(info);
                slaves.remove(info);
            }

        } finally {
            lock.unlock();
        }

    }

    public void recover(InstanceInfo info) {
        boolean isMaster = info.isMaster();
        lock.lock();
        try {
            if (isMaster && info.equals(canceledMaster)) {
                master = canceledMaster;
                canceledMaster = null;
            } else {
                slaves.add(info);
                canceledSlaves.remove(info);
            }

        } finally {
            lock.unlock();
        }

    }

    public List<InstanceInfo> getConfigedInfos() {
        return configedInfos;
    }

    public InstanceInfo getCanceledMaster() {
        return canceledMaster;
    }

    public List<InstanceInfo> getCanceledSlaves() {
        return canceledSlaves;
    }

    public InstanceInfo getMaster() {
        return master;
    }

    public List<InstanceInfo> getSlaves() {
        return slaves;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Shard [id=" + id + ", master=" + master + ", slaves=" + slaves
                + ", canceledSlaves=" + canceledSlaves + ", canceledMaster=" + canceledMaster
                + ", configedInfos=" + configedInfos + ", status=" + status + "]";
    }
}
