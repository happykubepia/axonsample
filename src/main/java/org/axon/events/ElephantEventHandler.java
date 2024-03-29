package org.axon.events;

import lombok.extern.slf4j.Slf4j;
import org.axon.command.BackToReadyCommand;
import org.axon.command.CreateEnterCountCommand;
import org.axon.command.UpdateEnterCountCommand;
import org.axon.dto.StatusEnum;
import org.axon.entity.Elephant;
import org.axon.repository.ElephantRepository;
import org.axon.repository.EnterCountRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.AllowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@ProcessingGroup("elephant")      //전체 Event Replay시 대상 class를 구별하기 위해 부여
@AllowReplay                    //Event Replay를 활성화 함. 비활성화할 EventHandler에는 @DisallowReplay를 지정
public class ElephantEventHandler {

    private final ElephantRepository elephantRepository;
    private transient final EventGateway eventGateway;
    private transient final CommandGateway commandGateway;
    private final EnterCountRepository enterCountRepository;
    @Autowired
    public ElephantEventHandler(ElephantRepository elephantRepository, EventGateway eventGateway,
                                CommandGateway commandGateway, EnterCountRepository enterCountRepository) {
        this.elephantRepository = elephantRepository;
        this.eventGateway = eventGateway;
        this.commandGateway = commandGateway;
        this.enterCountRepository = enterCountRepository;
    }

    @EventHandler
    private void on(CreatedElephantEvent event) {
        log.info("[@EventHandler] CreatedElephantEvent for Id: {}", event.getId());
        Elephant elephant = new Elephant();
        elephant.setId(event.getId());
        elephant.setName(event.getName());
        elephant.setWeight(event.getWeight());
        elephant.setStatus(event.getStatus());

        try {
            elephantRepository.save(elephant);

            //-- Count 생성 command
            commandGateway.send(CreateEnterCountCommand.builder()
                    .countId("COUNT_"+event.getId())
                    .elephantId(event.getId()).count(0).build());
        } catch(Exception e) {
            log.info(e.getMessage());
        }
    }

    @EventHandler
    private void on(EnteredElephantEvent event) {
        log.info("[@EventHandler] EnteredElephantEvent for Id: {}", event.getId());

        Elephant elephant = getEntity(event.getId());
        if(elephant != null) {
            elephant.setStatus(event.getStatus());
            elephantRepository.save(elephant);

            //무게가 100kg을 넘으면 실패 Event를 생성/발송함
            if(elephant.getWeight() > 100) {
                log.info("==== 100Kg 넘어서 넣기 실패! 실패 이벤트 발송!");
                eventGateway.publish(new FailedEnterElephantEvent(event.getId()));
                return;
            }

            //-- Count 갱신 command
            commandGateway.send(UpdateEnterCountCommand.builder()
                    .countId("COUNT_"+event.getId())
                    .elephantId(event.getId()).count(1).build());
        }
    }
    @EventHandler
    private void on(FailedEnterElephantEvent event) {
        log.info("[@EventHandler] FailedEnterElephantEvent for Id: {}", event.getId());
        //-- 보상처리 요청
        commandGateway.send(BackToReadyCommand.builder()
                .id(event.getId())
                .status(StatusEnum.READY.value())
                .build());
    }

    @EventHandler
    private void on(ExitedElephantEvent event) {
        log.info("[@EventHandler] ExitedElephantEvent for Id: {}", event.getId());
        Elephant elephant = getEntity(event.getId());
        if(elephant != null) {
            elephant.setStatus(event.getStatus());
            elephantRepository.save(elephant);
        }
    }

    @EventHandler
    private void on(BackToReadyCompletedEvent event) {
        log.info("[@EventHandler] BackToReadyCompletedEvent for Id: {}", event.getId());

        Elephant elephant = getEntity(event.getId());
        if(elephant != null) {
            elephant.setStatus(event.getStatus());
            elephantRepository.save(elephant);
        }
    }

    private Elephant getEntity(String id) {
        Optional<Elephant> optElephant = elephantRepository.findById(id);
        return optElephant.orElse(null);
    }

    //===================== 전체 이벤트 Replay하여 DB에 최종 상태 저장 ===========
    @ResetHandler
    private void replayAll() {
        log.info("[@ResetHandler] Executing replayAll");
        elephantRepository.deleteAll();
        enterCountRepository.deleteAll();
    }
}
