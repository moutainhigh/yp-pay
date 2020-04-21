//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.yp.pay.common.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalSysnoGenerator {
    private static final long EPOCH = 157680000000L;
    private static final int WORKER_ID_BITS = 10;
    private static final int MAX_WORKER_ID = 1023;
    private static final int CURRENT_WORKER_ID = (int)Math.abs(IPUtils.ipToLong(IPUtils.getLocalIp()) % 1023L);
    private int workerId;
    private long sequence = 0L;
    private int sequenceBits = 12;
    private int keepBits = 0;
    private int workerIdShift;
    private int timestampLeftShift;
    private long lastTimestamp;
    private int sequenceMask;
    private static final int MAX_OFFSET_WAIT = 10;
    private static GlobalSysnoGenerator instance = new GlobalSysnoGenerator();
    private static Map<Integer, GlobalSysnoGenerator> instanceMap = new ConcurrentHashMap(16);

    private GlobalSysnoGenerator() {
        this.workerIdShift = this.sequenceBits + this.keepBits;
        this.timestampLeftShift = 10 + this.workerIdShift;
        this.lastTimestamp = -1L;
        this.sequenceMask = ~(-1 << this.sequenceBits);
        this.workerId = CURRENT_WORKER_ID;
    }

    public static GlobalSysnoGenerator getInstance() {
        return instance;
    }

    public static GlobalSysnoGenerator getInstance(int workerId) {
        GlobalSysnoGenerator globalSysnoGenerator = (GlobalSysnoGenerator)instanceMap.get(workerId);
        if (globalSysnoGenerator == null) {
            globalSysnoGenerator = new GlobalSysnoGenerator();
            globalSysnoGenerator.setWorkerId(workerId);
            instanceMap.put(workerId, globalSysnoGenerator);
        }

        return globalSysnoGenerator;
    }

    public GlobalSysnoGenerator sequenceBits(int sequenceBits) {
        this.setSequenceBits(sequenceBits);
        return this;
    }

    public synchronized long nextSysno() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < this.lastTimestamp) {
            long offset = this.lastTimestamp - timestamp;
            if (offset > 10L) {
                throw new RuntimeException("时间产生回拨，id生成器不可用" + timestamp);
            }

            try {
                this.wait(offset << 1);
                timestamp = System.currentTimeMillis();
                if (timestamp < this.lastTimestamp) {
                    throw new RuntimeException("时间产生回拨，id生成器不可用" + timestamp);
                }
            } catch (InterruptedException var6) {
                throw new RuntimeException(var6);
            }
        }

        if (this.lastTimestamp == timestamp) {
            this.sequence = this.sequence + 1L & (long)this.sequenceMask;
            if (this.sequence == 0L) {
                timestamp = this.tilNextMillis(timestamp);
            }
        } else {
            this.sequence = 0L;
        }

        this.lastTimestamp = timestamp;
        return timestamp - 157680000000L << this.timestampLeftShift | (long)(this.workerId << this.sequenceBits + this.keepBits) | this.sequence << this.keepBits;
    }

    private long tilNextMillis(final long lastTimestamp) {
        long timestamp;
        for(timestamp = System.currentTimeMillis(); timestamp <= lastTimestamp; timestamp = System.currentTimeMillis()) {
        }

        return timestamp;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public long getWorkerId() {
        return (long)this.workerId;
    }

    public int getSequenceBits() {
        return this.sequenceBits;
    }

    public void setSequenceBits(int sequenceBits) {
        this.sequenceBits = sequenceBits;
    }

    public int getKeepBits() {
        return this.keepBits;
    }

    public void setKeepBits(int keepBits) {
        this.keepBits = keepBits;
    }

    public static void main(String[] args) {
        GlobalSysnoGenerator generator = getInstance();
        generator.nextSysno();
        GlobalSysnoGenerator workder2 = getInstance(125);
        int length = 100;
        long st = System.currentTimeMillis();
        Set<Long> set = new HashSet();

        for(int i = 0; i < length; ++i) {
            long sysno = generator.nextSysno();
            set.add(sysno);
            System.out.println(sysno);
            System.out.println(Long.toBinaryString(sysno) + " \t " + Long.toBinaryString(sysno).length());
        }

        long elapsed = System.currentTimeMillis() - st;
        System.out.println(elapsed + "毫秒");
    }
}
