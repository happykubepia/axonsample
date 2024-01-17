package org.axon.events;
/*
- 목적: Entity를 정의하며 JPA(Java Persistence API)로 CRUD할 Table구조를 정의
*/

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FailedEnterElephantEvent {
    private String id;
}
