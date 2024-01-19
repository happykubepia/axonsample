package org.axon.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatedEnterCountEvent {
    private String countId;
    private String elephantId;
    private int count;
}
