package org.axon.queries;

import lombok.extern.slf4j.Slf4j;
import org.axon.entity.Elephant;
import org.axon.repository.ElephantRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class ElephantQueryHandler {

    private final ElephantRepository elephantRepository;
    @Autowired
    public ElephantQueryHandler(ElephantRepository elephantRepository) {
        this.elephantRepository = elephantRepository;
    }
    //-- 코끼리 리스트를 리턴
    @QueryHandler(queryName = "list")
    private List<Elephant> getElephants(String dummy) {
        Optional<List<Elephant>> optElephants = Optional.of(elephantRepository.findAll());
        return optElephants.orElse(null);
    }

    //-- 코끼리 정보 리턴
    @QueryHandler
    private Elephant getElephant(GetElephantQuery query) {
        Optional<Elephant> optElephant = elephantRepository.findById(query.getId());
        return optElephant.orElse(null);
    }
}
