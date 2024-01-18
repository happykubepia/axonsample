package org.axon.service;

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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ElephantService {
    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;

    private final ElephantRepository elephantRepository;
    @Autowired
    public ElephantService(ElephantRepository elephantRepository) {
        this.elephantRepository = elephantRepository;
    }

    public ResultVO<CreateElephantCommand> create(ElephantDTO elephant) {
        log.info("[ElephantService] Executing create: {}", elephant.toString());

        ResultVO<CreateElephantCommand> retVo = new ResultVO<>();

        //check validation
        if(elephant.getWeight() < 30 || elephant.getWeight() > 200) {
            retVo.setReturnCode(false);
            retVo.setReturnMessage("몸무게는 30kg이상 200kb이하로 입력해 주세요.");
            return retVo;
        }

        //send command
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

    public ResultVO<String> enter(String id) {
        log.info("[ElephantService] Executing enter for Id: {}", id);
        ResultVO<String> retVo = new ResultVO<>();

        //-- check validation
        Elephant elephant = getEntity(id);
        if(elephant.getStatus().equals(StatusEnum.ENTER.value())) {
            retVo.setReturnCode(false);
            retVo.setReturnMessage("이미 냉장고 안에 있는 코끼리입니다.");
            return retVo;
        }
        //--send command
        try {
            commandGateway.sendAndWait(EnterElephantCommand.builder()
                    .id(id)
                    .status(StatusEnum.ENTER.value())
                    .build(), 30, TimeUnit.SECONDS);

            retVo.setReturnCode(true);
            retVo.setReturnMessage("Success to request enter elephant");
        } catch(Exception e) {
            retVo.setReturnCode(false);
            retVo.setReturnMessage(e.getMessage());
        }
        return retVo;
    }


    public ResultVO<String> exit(String id) {
        log.info("[ElephantService] Executing exit for Id: {}", id);
        ResultVO<String> retVo = new ResultVO<>();

        //-- check validation
        Elephant elephant = getEntity(id);
        if(!elephant.getStatus().equals(StatusEnum.ENTER.value())) {
            retVo.setReturnCode(false);
            retVo.setReturnMessage("냉장고 안에 있는 코끼리만 꺼낼 수 있습니다.");
            return retVo;
        }

        //-- send command
        try {
            commandGateway.sendAndWait(ExitElephantCommand.builder()
                    .id(id)
                    .status(StatusEnum.EXIT.value())
                    .build(), 30, TimeUnit.SECONDS);

            retVo.setReturnCode(true);
            retVo.setReturnMessage("Success to request exit elephant");
        } catch(Exception e) {
            retVo.setReturnCode(false);
            retVo.setReturnMessage(e.getMessage());
        }
        return retVo;
    }

    public ResultVO<List<Elephant>> getLists() {
        log.info("[ElephantService] Executing getLists");

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

    public ResultVO<Elephant> getElephant(String id) {
        log.info("[ElephantService] Executing getElephant for Id: {}", id);

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

    public ResultVO<Elephant> deleteElephant(String id) {
        log.info("[@DeleteMapping '/elephant/{id}'] Id: {}", id);

        ResultVO<Elephant> retVo = new ResultVO<>();
        Elephant elephant = getEntity(id);
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

    private Elephant getEntity(String id) {
        Optional<Elephant> optElephant = elephantRepository.findById(id);
        return optElephant.orElse(null);
    }
}
