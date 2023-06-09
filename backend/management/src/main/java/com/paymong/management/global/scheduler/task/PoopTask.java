package com.paymong.management.global.scheduler.task;

import com.paymong.management.global.code.MongActiveCode;
import com.paymong.management.global.code.MongConditionCode;
import com.paymong.management.global.code.WebSocketCode;
import com.paymong.management.global.exception.NotFoundMongException;
import com.paymong.management.global.exception.UnsuitableException;
import com.paymong.management.global.socket.service.WebSocketService;
import com.paymong.management.history.entity.ActiveHistory;
import com.paymong.management.history.repository.ActiveHistoryRepository;
import com.paymong.management.mong.entity.Mong;
import com.paymong.management.mong.repository.MongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PoopTask {

    private final MongRepository mongRepository;
    private final ActiveHistoryRepository activeHistoryRepository;
    private final WebSocketService webSocketService;
    @Transactional
    public void addPoop(Long mongId) throws NotFoundMongException, UnsuitableException {

        Mong mong = mongRepository.findByMongIdAndActive(mongId, true)
                .orElseThrow(() -> new NotFoundMongException());

        if(mong.getStateCode().equals(MongConditionCode.SLEEP.getCode()) ||
                mong.getStateCode().equals(MongConditionCode.DIE.getCode()) ||
                mong.getStateCode().equals(MongConditionCode.GRADUATE.getCode())){
            throw new UnsuitableException();
        }
        Integer poop = mong.getPoopCount();
        Integer penalty = mong.getPenalty();
        if(poop == 4){
            // 패널티 적립
            mong.setPenalty(penalty + 1);
            if(mong.getStateCode().equals(MongConditionCode.NORMAL.getCode())
            || mong.getStateCode().equals(MongConditionCode.SOMNOLENCE.getCode())
            || mong.getStateCode().equals(MongConditionCode.HUNGRY.getCode())) {
                mong.setStateCode(MongConditionCode.SICK.getCode());
            }
            ActiveHistory activeHistory = ActiveHistory.builder()
                    .activeCode(MongActiveCode.PENALTY.getCode())
                    .activeTime(LocalDateTime.now())
                    .mongId(mongId)
                    .build();

            activeHistoryRepository.save(activeHistory);
        }else{

            // 갯수 적립
            mong.setPoopCount(poop + 1);

            ActiveHistory activeHistory = ActiveHistory.builder()
                    .activeCode(MongActiveCode.BOWEL.getCode())
                    .activeTime(LocalDateTime.now())
                    .mongId(mongId)
                    .build();

            activeHistoryRepository.save(activeHistory);
        }
        if(poop >= 3){
            webSocketService.sendStatus(mong, WebSocketCode.POOP);
        }else{
            webSocketService.sendStatus(mong, WebSocketCode.SUCCESS);
        }

    }
}
