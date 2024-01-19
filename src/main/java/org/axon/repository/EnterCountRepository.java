package org.axon.repository;

import org.axon.entity.EnterCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnterCountRepository extends JpaRepository<EnterCount, String> {

}
