package com.okex.open.api.websocket;

import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityStrategy;
import net.openhft.affinity.AffinityThreadFactory;

import java.util.concurrent.ThreadFactory;


public class CustomAffThreadFactory implements ThreadFactory {
    private final String name;
    private final boolean daemon;
    private final AffinityStrategy[] strategies;
    private AffinityLock lastAffinityLock = null;
    private int id = 1;

    public CustomAffThreadFactory(String name, boolean daemon, AffinityStrategy... strategies) {
        this.name = name;
        this.daemon = daemon;
        this.strategies = strategies.length == 0 ? new AffinityStrategy[]{AffinityStrategies.ANY} : strategies;
    }

    public CustomAffThreadFactory(String affThreadName, AffinityStrategies affinityStrategies) {
        this(affThreadName, true, affinityStrategies);
    }

    public Boolean releaseAll() {
        lastAffinityLock.release();
        lastAffinityLock = null;
        return true;
    }

    @Override
    public synchronized Thread newThread(final Runnable r) {
        String name2 = id <= 1 ? name : (name + '-' + id);
        id++;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                AffinityLock allock = acquireLockBasedOnLast();
                try {
                    r.run();
                } catch (Exception exception) {
                    System.out.println("Thread is terminating gracefully.");
                    allock.release();
                } finally {
                    System.out.println("Thread is terminating gracefully.");
                    allock.release();
                }
            }
        }, name2);
        t.setDaemon(daemon);
        return t;
    }

    private synchronized AffinityLock acquireLockBasedOnLast() {
        AffinityLock al = lastAffinityLock == null ? AffinityLock.acquireLock(false) : lastAffinityLock.acquireLock(strategies);
        al.bind();
        if (al.cpuId() >= 0)
            lastAffinityLock = al;
        return al;
    }


}
