package org.axon.aggregate;

import lombok.extern.slf4j.Slf4j;
import org.axon.command.BackToReadyCommand;
import org.axon.command.CreateElephantCommand;
import org.axon.command.EnterElephantCommand;
import org.axon.command.ExitElephantCommand;
import org.axon.dto.StatusEnum;
import org.axon.events.BackToReadyCompletedEvent;
import org.axon.events.CreatedElephantEvent;
import org.axon.events.EnteredElephantEvent;
import org.axon.events.ExitedElephantEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
//@Aggregate
@Aggregate(snapshotTriggerDefinition = "snapshotTrigger", cache="snapshotCache")
public class ElephantAggregate {
    @AggregateIdentifier
    private String id;
    @AggregateMember
    private String name;
    @AggregateMember
    private int weight;
    @AggregateMember
    private String status;

    @Autowired
    private transient CommandGateway commandGateway;

    public ElephantAggregate() {}

    //-- 새로운 코끼리 객체 생성 처리
    @CommandHandler
    private ElephantAggregate(CreateElephantCommand cmd) {
        log.info("[@CommandHandler] CreateElephantCommand for Id: {}", cmd.getId());
        CreatedElephantEvent event = new CreatedElephantEvent(
                cmd.getId(), cmd.getName(), cmd.getWeight(), cmd.getStatus());
        AggregateLifecycle.apply(event);
    }
    @EventSourcingHandler
    private void on(CreatedElephantEvent event) {
        log.info("[@EventSourcingHandler] CreatedElephantEvent for Id: {}", event.getId());
        this.id = event.getId();
        this.name = event.getName();
        this.weight = event.getWeight();
        this.status = event.getStatus();
    }

    //-- 냉장고에 넣기
    @CommandHandler
    private void handle(EnterElephantCommand cmd) {
        log.info("[@CommandHandler] EnterElephantCommand for Id: {}", cmd.getId());

        AggregateLifecycle.apply(new EnteredElephantEvent(cmd.getId(), StatusEnum.ENTER.value()));
    }
    @EventSourcingHandler
    private void on(EnteredElephantEvent event) {
        log.info("[@EventSourcingHandler] EnteredElephantEvent for Id: {}", event.getId());
        log.info("======== [넣기] Event Replay => 코끼리 상태: {}", this.status);

        this.status = event.getStatus();

        log.info("======== [넣기] 최종 코끼리 상태: {}", this.status);

    }

    //-- 냉장고에서 꺼내기
    @CommandHandler
    private void handle(ExitElephantCommand cmd) {
        log.info("[@CommandHandler] ExitElephantCommand for Id: {}", cmd.getId());

        AggregateLifecycle.apply(new ExitedElephantEvent(cmd.getId(), StatusEnum.EXIT.value()));
    }
    @EventSourcingHandler
    private void on(ExitedElephantEvent event) {
        log.info("[@EventSourcingHandler] ExitedElephantEvent for Id: {}", event.getId());
        log.info("======== [꺼내기] Event Replay => 코끼리 상태: {}", this.status);

        this.status = event.getStatus();

        log.info("======== [꺼내기] 최종 코끼리 상태: {}", this.status);

    }

    //-- 냉장고 넣기 실패 시 보상처리: 상태를 다시 'Ready'로 변경함
    @CommandHandler
    private void handle(BackToReadyCommand cmd) {
        log.info("[@CommandHandler] BackToReadyCommand for Id: {}", cmd.getId());

       AggregateLifecycle.apply(new BackToReadyCompletedEvent(cmd.getId(), cmd.getStatus()));
    }
    @EventSourcingHandler
    private void on(BackToReadyCompletedEvent event) {
        log.info("[@EventSourcingHandler] BackToReadyCompletedEvent for Id: {}", event.getId());

        this.status = event.getStatus();
    }
}
