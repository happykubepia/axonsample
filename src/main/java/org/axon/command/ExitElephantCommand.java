package org.axon.command;
/*
- 목적: Entity를 정의하며 JPA(Java Persistence API)로 CRUD할 Table구조를 정의
*/

import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
@Builder
public class ExitElephantCommand {
    @TargetAggregateIdentifier
    String id;
    String status;
}
