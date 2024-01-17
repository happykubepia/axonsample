package org.axon.events;
/*
- 목적: Entity를 정의하며 JPA(Java Persistence API)로 CRUD할 Table구조를 정의
*/

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.axon.dto.StatusEnum;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
public class EnteredElephantEvent {
    private String id;
    private String status;
}
