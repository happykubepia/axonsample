package org.axon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.axon.command.CreateElephantCommand;
import org.axon.dto.ElephantDTO;
import org.axon.entity.Elephant;
import org.axon.service.ElephantService;
import org.axon.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Order service API", description="Order service API" )
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class APIController {

    private final ElephantService elephantService;

    @Autowired
    public APIController(ElephantService elephantService) {
        this.elephantService = elephantService;
    }

    @PostMapping("/create")
    @Operation(summary = "코끼리 생성 API")
    private ResultVO<CreateElephantCommand> create(@RequestBody ElephantDTO elephant) {
        log.info("[@PostMapping '/create'] Executing create: {}", elephant.toString());

        return elephantService.create(elephant);
    }


    @PostMapping("/enter/{id}")
    @Operation(summary = "냉장고에 넣기 API")
    @Parameters({
            @Parameter(name = "id", in= ParameterIn.PATH, description = "코끼리ID", required = true)
    })
    private ResultVO<String> enter(@PathVariable(name = "id") String id) {
        log.info("[@PostMapping '/enter'] Id: {}", id);

        return elephantService.enter(id);
    }

    @PostMapping("/exit/{id}")
    @Operation(summary = "냉장고에서 꺼내기 API")
    @Parameters({
            @Parameter(name = "id", in= ParameterIn.PATH, description = "코끼리ID", required = true)
    })
    private ResultVO<String> exit(@PathVariable(name = "id") String id) {
        log.info("[@PostMapping '/exit'] Id: {}", id);

        return elephantService.exit(id);
    }

    @GetMapping("/elephants")
    @Operation(summary = "코끼리 리스트 API")
    private ResultVO<List<Elephant>> getLists() {
        log.info("[@GetMapping '/elephants']");

        return elephantService.getLists();
    }

    @GetMapping("/elephant/{id}")
    @Operation(summary = "코끼리 정보 API")
    @Parameters({
            @Parameter(name = "id", in= ParameterIn.PATH, description = "코끼리ID", required = true)
    })
    private ResultVO<Elephant> getElephant(@PathVariable(name = "id") String id) {
        log.info("[@GetMapping '/elephant/{id}'] Id: {}", id);

        return elephantService.getElephant(id);
    }

    @DeleteMapping("/elephant/{id}")
    @Operation(summary = "코끼리 삭제 API")
    @Parameters({
            @Parameter(name = "id", in= ParameterIn.PATH, description = "코끼리ID", required = true)
    })
    private ResultVO<Elephant> deleteElephant(@PathVariable(name = "id") String id) {
        log.info("[@DeleteMapping '/elephant/{id}'] Id: {}", id);

        return elephantService.deleteElephant(id);
    }
}
