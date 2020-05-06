package com.yp.pay.common.util;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局唯一id生成器
 * @author huchao
 * @date 2019/1/22
 *
 * GlobalSysNoGenerator 改为 GlobalSysnoGenerator
 * @date 2019/04/03
 */
@Service
public class GlobalSysnoGenerator {

    /**
     * 时间戳回推5年
     */
    private final static long EPOCH = 5 * 365 * 24 * 3600 * 1000L;


    /**
     * 工作机器位数，默认为10位，支持1024台机器
     */
    private final static int WORKER_ID_BITS = 10;

    /**
     * 最大workerId
     */
    private final static int MAX_WORKER_ID = ~(-1 << WORKER_ID_BITS);

    /**
     * 本机所对应workId，当前根据服务器ip计算得出
     */
    private final static int CURRENT_WORKER_ID = (int) Math.abs((IPUtils.ipToLong(IPUtils.getLocalIp()) % MAX_WORKER_ID));

    private int workerId;

    /**
     * 序列号，初始为0
     */
    private long sequence = 0L;

    /**
     * 递增序列位数，默认为12位，每毫秒支持4096个序列
     */
    private int sequenceBits = 12;

    /**
     * 保留位，未启用
     */
    private int keepBits = 0;

    private int workerIdShift = sequenceBits + keepBits;

    /**
     * timestamp 41位,大概可用69年
     */
    private int timestampLeftShift = WORKER_ID_BITS + workerIdShift;

    private long lastTimestamp = -1L;

    private int sequenceMask =  ~(-1 << sequenceBits);
    /**
     * 回拨时间最大差值等待时间（10ms，等待20毫秒）
     */
    private final static int MAX_OFFSET_WAIT = 10;

    private static GlobalSysnoGenerator instance = new GlobalSysnoGenerator();

    private static Map<Integer, GlobalSysnoGenerator> instanceMap = new ConcurrentHashMap<>(16);

    private GlobalSysnoGenerator() {
        workerId = GlobalSysnoGenerator.CURRENT_WORKER_ID;
    }

    public static GlobalSysnoGenerator getInstance(){
        return instance;
    }

    public static GlobalSysnoGenerator getInstance(int workerId){
        GlobalSysnoGenerator globalSysnoGenerator = instanceMap.get(workerId);
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

    /**
     * 64bit 整型数
     * 组成       符号     时间戳(固定41位，69年) workid(默认10，支持 1024台机器) 序列号(默认12位，4096序列/ms)
     * ----      ------           ------               -----                     -----
     * 位数       1               41                     10                        12
     * @return long型整数
     */
    public synchronized long nextSysno() {
        long timestamp = System.currentTimeMillis();
        /**
         * 出现时间回拨
         */
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= MAX_OFFSET_WAIT) {
                try {
                    //时间偏差大小小于10ms，则等待两倍时间
                    wait(offset << 1);
                    timestamp = System.currentTimeMillis();
                    if (timestamp < lastTimestamp) {
                        //还是小于，抛异常并上报
                        throw new RuntimeException("时间产生回拨，id生成器不可用" + timestamp);
                    }
                } catch (InterruptedException e) {
                    throw  new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("时间产生回拨，id生成器不可用" + timestamp);
            }
        }

        /**
         * 时间戳相等，递增序列号
         */
        if (this.lastTimestamp == timestamp) {
            this.sequence = (this.sequence + 1L) & sequenceMask;
            if (this.sequence == 0L) {
                timestamp = this.tilNextMillis(timestamp);
            }
        } else {
            this.sequence = 0L;
        }
        this.lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << timestampLeftShift)
                | (workerId << (sequenceBits + keepBits))
                | (this.sequence << keepBits);
    }

    /**
     * 等待到下一个时间戳
     * @param lastTimestamp
     * @return
     */
    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public long getWorkerId() {
        return workerId;
    }

    public int getSequenceBits() {
        return sequenceBits;
    }

    public void setSequenceBits(int sequenceBits) {
        this.sequenceBits = sequenceBits;
    }

    public int getKeepBits() {
        return keepBits;
    }

    public void setKeepBits(int keepBits) {
        this.keepBits = keepBits;
    }

    public static void main(String[] args) {
        GlobalSysnoGenerator generator = GlobalSysnoGenerator.getInstance();
        generator.nextSysno();
        GlobalSysnoGenerator workder2 = GlobalSysnoGenerator.getInstance(125);
        int length = 100;
        long st = System.currentTimeMillis();
        Set<Long> set = new HashSet<>();
        for (int i = 0; i < length; i++) {
            long sysno = generator.nextSysno();
            set.add(sysno);
            System.out.println(sysno);
            System.out.println(Long.toBinaryString(sysno)+" \t "+Long.toBinaryString(sysno).length());
        }
        long elapsed = System.currentTimeMillis() - st;
        System.out.println(elapsed + "毫秒");
    }
}