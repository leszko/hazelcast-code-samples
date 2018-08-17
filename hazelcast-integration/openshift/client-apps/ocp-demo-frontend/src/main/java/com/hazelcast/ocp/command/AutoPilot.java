package com.hazelcast.ocp.command;

import com.hazelcast.core.IMap;
import com.hazelcast.util.Preconditions;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class AutoPilot {
    private static final Logger log = Logger.getLogger(AutoPilot.class);

    private final int readCount;
    private final int insertCount;
    private final int poolSize;
    private final int valueSize;
    private final IMap<String, String> map;

    private ExecutorService executorService;
    private List<Callable<Integer>> callables;

    private AutoPilot(int readCount, int insertCount, int poolSize, int valueSize,
              IMap<String, String> map) {
        this.readCount = readCount;
        this.insertCount = insertCount;
        this.poolSize = poolSize;
        this.valueSize = valueSize;
        this.map = map;
    }

    void start() {
        Preconditions.checkNotNull(map);
        Preconditions.checkPositive(poolSize, "invalid pool size");

        initialize();
        log.info(String.format("Auto pilot started with %s number of threads", poolSize));

        try {
            executorService.invokeAll(callables)
                           .stream()
                           .map(f -> {
                               try {
                                   return f.get();
                               } catch (Exception e) {
                                   log.error(e.getMessage(), e);
                                   throw new IllegalStateException(e);
                               }
                           })
                           .forEach(it -> log.info(String.format("Map Size = %s", it)));

            executorService.shutdown();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    private void initialize() {
        executorService = Executors.newFixedThreadPool(poolSize);

        List<Callable<Integer>> inserts = IntStream.range(0, insertCount)
                                                   .boxed()
                                                   .map(it -> (Callable<Integer>) () -> {
                                                       String key = RandomStringUtils.randomAlphanumeric(42);
                                                       String value = RandomStringUtils.randomAlphabetic(valueSize);
                                                       map.put(key, value);
                                                       return map.size();
                                                   })
                                                   .collect(Collectors.toList());

        List<Callable<Integer>> reads = IntStream.range(0, readCount).boxed()
                                                 .map(it -> (Callable<Integer>) () -> {
                                                     String key = RandomStringUtils.randomAlphanumeric(42);
                                                     String value = map.get(key);
                                                     log.info(String.format("randomly get value : %s size: %s", value,
                                                             map.size()));
                                                     return map.size();
                                                 })
                                                 .collect(Collectors.toList());

        callables = new ArrayList<>();
        callables.addAll(reads);
        callables.addAll(inserts);

        Collections.shuffle(callables);
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private int readCount;
        private int insertCount;
        private int poolSize;
        private int valueSize;
        private IMap<String, String> map;

        Builder readCount(int readCount) {
            this.readCount = readCount;
            return this;
        }

        Builder insertCount(int insertCount) {
            this.insertCount = insertCount;
            return this;
        }

        Builder poolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        Builder valueSize(int valueSize) {
            this.valueSize = valueSize;
            return this;
        }

        Builder map(IMap<String, String> map) {
            this.map = map;
            return this;
        }

        AutoPilot build() {
            return new AutoPilot(readCount, insertCount, poolSize, valueSize, map);
        }
    }
}
