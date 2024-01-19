package org.axon.command;

import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
@Builder
public class CreateEnterCountCommand {
    @TargetAggregateIdentifier
    String countId;
    String elephantId;
    int count;
}
