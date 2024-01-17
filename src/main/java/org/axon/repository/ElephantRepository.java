package org.axon.repository;

import org.axon.entity.Elephant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElephantRepository extends JpaRepository<Elephant, String> {

}