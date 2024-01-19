package org.axon.entity;
/*
- 목적: Entity를 정의하며 JPA(Java Persistence API)로 CRUD할 Table구조를 정의
*/

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.axon.command.CreateEnterCountCommand;
import org.axon.command.UpdateEnterCountCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;

import java.io.Serial;
import java.io.Serializable;

@Aggregate
@Data
@Entity
@Slf4j
@Table(name="enter_count")
public final class EnterCount implements Serializable {
    /*
    Serialize(DB에 저장 시 binary로 변환)와 Deserialize(DB에 저장된 binary 데이터를 원래 값으로 변환)시 사용할 UID
    'serialVersionUID에 마우스를 올려놓고 전구 아이콘 메뉴에서 <Randomly change 'serialVersionUID' initializer> 선택
    */
    @Serial
    private static final long serialVersionUID = 1182092962825835024L;

    @AggregateIdentifier
    @Id     //Primary key 필드를 나타냄
    @Column(name="count_id", nullable = false, length = 10)
    private String countId;

    @AggregateMember
    @Column(name="elephant_id", nullable = false, length = 3)
    private String elephantId;

    @AggregateMember
    @Column(name="count", nullable = false)
    private int count;

    public EnterCount() { }

    @CommandHandler
    private EnterCount(CreateEnterCountCommand cmd) {
        log.info("[@CommandHandler] CreateEnterCountCommand for Id: {}", cmd.getElephantId());

        //-- 새로운 Entity 등록(테이블에 바로 등록됨)
        this.countId = cmd.getCountId();
        this.elephantId = cmd.getElephantId();
        this.count = cmd.getCount();
    }

    @CommandHandler
    private void handle(UpdateEnterCountCommand cmd) {
        log.info("[@CommandHandler] UpdateEnterCountCommand for Id: {}", cmd.getElephantId());

        //-- Entity 최종 상태 갱신(테이블에 바로 저장됨)
        log.info("current count: {} + {}", this.count, cmd.getCount());
        this.count += cmd.getCount();
    }
}
