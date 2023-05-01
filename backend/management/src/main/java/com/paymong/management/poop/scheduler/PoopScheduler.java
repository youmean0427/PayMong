package com.paymong.management.poop.scheduler;

import com.paymong.management.global.exception.NotFoundMongException;
import com.paymong.management.global.scheduler.ManagementScheduler;
import com.paymong.management.poop.service.PoopService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PoopScheduler implements ManagementScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoopScheduler.class);
    private static final Map<Long, ThreadPoolTaskScheduler> schedulerMap = new HashMap<>();
    private final PoopService poopService;

    @Override
    public void stopScheduler(Long mongId){
        LOGGER.info("{}의 {} scheduler를 중지합니다.", this.getClass().getSimpleName(), mongId);
        schedulerMap.get(mongId).shutdown();
    }

    @Override
    public void startScheduler(Long mongId){
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
//        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//      scheduler.setPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        scheduler.setThreadNamePrefix("poop-" + mongId + "-");
        // 스케쥴러 시작
        LOGGER.info("new {}를 추가합니다.", this.getClass().getSimpleName());
        scheduler.schedule(getRunnable(mongId), Date.from(Instant.now().plusSeconds(getDelay())));
        schedulerMap.put(mongId, scheduler);
    }

    @Override
    public Runnable getRunnable(Long mongId) {
        return () -> {
            try {
                poopService.addPoop(mongId);
                stopAndRunScheduler(mongId);
            } catch (NotFoundMongException e) {
                stopScheduler(mongId);
            }

        };
    }

    @Override
    public Trigger getTrigger() {
        return new PeriodicTrigger(5, TimeUnit.SECONDS);
    }

    @Override
    public Long getDelay() {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        long p = random.nextInt(31)+30;
        return p*60L;
    }

    public void stopAndRunScheduler(Long mongId){
        stopScheduler(mongId);
        startScheduler(mongId);
    }

}
