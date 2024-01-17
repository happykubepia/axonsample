package org.axon.entity;
/*
- 목적: Entity를 정의하며 JPA(Java Persistence API)로 CRUD할 Table구조를 정의
*/
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@Table(name="elephant")
public final class Elephant implements Serializable {
    /*
    Serialize(DB에 저장 시 binary로 변환)와 Deserialize(DB에 저장된 binary 데이터를 원래 값으로 변환)시 사용할 UID
    'serialVersionUID에 마우스를 올려놓고 전구 아이콘 메뉴에서 <Randomly change 'serialVersionUID' initializer> 선택
    */
    @Serial
    private static final long serialVersionUID = 3867844350539085719L;

    @Id     //Primary key 필드를 나타냄
    @Column(name="id", nullable = false, length = 3)
    private String id;

    @Column(name="name", nullable = false, length = 30)
    private String name;
    @Column(name="weight", nullable = false)
    private int weight;

    @Column(name="status", nullable = false, length = 20)
    private String status;
}
