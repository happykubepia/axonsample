package org.axon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.axon.command.CreateElephantCommand;
import org.axon.command.EnterElephantCommand;
import org.axon.command.ExitElephantCommand;
import org.axon.dto.ElephantDTO;
import org.axon.dto.StatusEnum;
import org.axon.entity.Elephant;
import org.axon.queries.GetElephantQuery;
import org.axon.repository.ElephantRepository;
import org.axon.vo.ResultVO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Tag(name = "Order service API", description="Order service API" )
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class APIController {
    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;
    @Autowired
    private ElephantRepository elephantRepository;

    @PostMapping("/create")
    @Operation(summary = "코끼리 생성 API")
    private ResultVO<CreateElephantCommand> create(@RequestBody ElephantDTO elephant) {
        log.info("[@PostMapping '/create'] Executing create: {}", elephant.toString());

        ResultVO<CreateElephantCommand> retVo = new ResultVO<>();
        CreateElephantCommand cmd = CreateElephantCommand.builder()
                .id(RandomStringUtils.random(3, false, true))
                .name(elephant.getName())
                .weight(elephant.getWeight())
                .status(StatusEnum.READY.value())
                .build();

        try {
            commandGateway.sendAndWait(cmd, 30, TimeUnit.SECONDS);
            retVo.setReturnCode(true);
            retVo.setReturnMessage("Success to create elephant");
            retVo.setResult(cmd);
        } catch(Exception e) {
            retVo.setReturnCode(false);
            retVo.setReturnMessage(e.getMessage());
        }

        return retVo;
    }


    @PostMapping("/enter/{id}")
    @Operation(summary = "냉장고에 넣기 API")
    @Parameters({
            @Parameter(name = "id", in= ParameterIn.PATH, description = "코끼리ID", required = true, allowEmptyValue = false)
    })
    private ResultVO<String> enter(@PathVariable(name = "id") String id) {
        log.info("[@PostMapping '/enter'] Id: {}", id);
        ResultVO<String> retVo = new ResultVO<>();

        //-- check validation
        Optional<Elephant> optElephant = elephantRepository.findById(id);
        Elephant elephant = optElephant.isPresent() ? optElephant.get() : null;
        if(!(elephant.getStatus().equals(StatusEnum.READY.value()) ||
                elephant.getStatus().equals(StatusEnum.EXIT.value()))) {
            retVo.setReturnCode(false);
            retVo.setReturnMessage("현재 코끼리 상태는 " + elephant.getStatus()+ ". 코끼리 상태가 'Ready' 또는 'Exit'인 경우만 넣을 수 있습니다.");
            return retVo;
        }
        //--send command
        try {
            commandGateway.sendAndWait(EnterElephantCommand.builder().id(id).build(), 30, TimeUnit.SECONDS);
            retVo.setReturnCode(true);
            retVo.setReturnMessage("Success to request enter elephant");
        } catch(Exception e) {
            retVo.setReturnCode(false);
            retVo.setReturnMessage(e.getMessage());
        }
        return retVo;
    }

    @PostMapping("/exit/{id}")
    @Operation(summary = "냉장고에서 꺼내기 API")
    @Parameters({
            @Parameter(name = "id", in= ParameterIn.PATH, description = "코끼리ID", required = true, allowEmptyValue = false)
    })
    private ResultVO<String> exit(@PathVariable(name = "id") String id) {
        log.info("[@PostMapping '/exit'] Id: {}", id);
        ResultVO<String> retVo = new ResultVO<>();

        //-- check validation
        Optional<Elephant> optElephant = elephantRepository.findById(id);
        Elephant elephant = optElephant.isPresent() ? optElephant.get() : null;
        if(!elephant.getStatus().equals(StatusEnum.ENTER.value())) {
            retVo.setReturnCode(false);
            retVo.setReturnMessage("현재 코끼리 상태는 " + elephant.getStatus()+ ". 코끼리 상태가 'Enter'인 경우만 꺼낼 수 있습니다.");
            return retVo;
        }

        //-- send command
        try {
            commandGateway.sendAndWait(ExitElephantCommand.builder().id(id).build(), 30, TimeUnit.SECONDS);
            retVo.setReturnCode(true);
            retVo.setReturnMessage("Success to request exit elephant");
        } catch(Exception e) {
            retVo.setReturnCode(false);
            retVo.setReturnMessage(e.getMessage());
        }
        return retVo;
    }

    @GetMapping("/elephants")
    @Operation(summary = "코끼리 리스트 API")
    private ResultVO<List<Elephant>> getLists() {
        log.info("[@GetMapping '/elephants']");

        ResultVO<List<Elephant>> retVo = new ResultVO<>();
        List<Elephant> elephants = queryGateway.query("list", "",
                ResponseTypes.multipleInstancesOf(Elephant.class)).join();
        if(elephants != null) {
            retVo.setReturnCode(true);
            retVo.setReturnMessage("코끼리수: "+elephants.size());
            retVo.setResult(elephants);
        } else {
            retVo.setReturnCode(false);
            retVo.setReturnMessage("No registered elephant");
        }
        return retVo;
    }

    @GetMapping("/elephant/{id}")
    @Operation(summary = "코끼리 정보 API")
    @Parameters({
            @Parameter(name = "id", in= ParameterIn.PATH, description = "코끼리ID", required = true, allowEmptyValue = false)
    })
    private ResultVO<Elephant> getElephant(@PathVariable(name = "id") String id) {
        log.info("[@GetMapping '/elephant/{id}'] Id: {}", id);

        ResultVO<Elephant> retVo = new ResultVO<>();
        Elephant elephant = queryGateway.query(new GetElephantQuery(id),
                ResponseTypes.instanceOf(Elephant.class)).join();
        if(elephant != null) {
            retVo.setReturnCode(true);
            retVo.setReturnMessage("ID: "+ id);
            retVo.setResult(elephant);
        } else {
            retVo.setReturnCode(false);
            retVo.setReturnMessage("Can't get elephant for Id:"+id);
        }
        return retVo;
    }

    @DeleteMapping("/elephant/{id}")
    @Operation(summary = "코끼리 삭제 API")
    @Parameters({
            @Parameter(name = "id", in= ParameterIn.PATH, description = "코끼리ID", required = true, allowEmptyValue = false)
    })
    private ResultVO<Elephant> deleteElephant(@PathVariable(name = "id") String id) {
        log.info("[@DeleteMapping '/elephant/{id}'] Id: {}", id);

        ResultVO<Elephant> retVo = new ResultVO<>();
        Optional<Elephant> optElephant = elephantRepository.findById(id);
        Elephant elephant = optElephant.isPresent() ? optElephant.get() : null;
        if(elephant != null) {
            elephantRepository.delete(elephant);
            retVo.setReturnCode(true);
            retVo.setReturnMessage("Elephant ID: "+ id + " is DELETED!");
            retVo.setResult(elephant);
        } else {
            retVo.setReturnCode(false);
            retVo.setReturnMessage("Can't get elephant for Id:"+id);
        }
        return retVo;
    }
}
