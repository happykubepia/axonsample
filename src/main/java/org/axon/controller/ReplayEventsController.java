package org.axon.controller;
/*
- 목적: Event를 replay하여 최종 상태를 DB에 저장함
*/
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@Tag(name = "Replay Events API")
@Slf4j
@RestController
@RequestMapping("/api/admin")
class ReplayEventsController {
    private final Configuration configuration;

    public ReplayEventsController(Configuration configuration) {
        this.configuration = configuration;
    }

    //-- 특정 EventHandler class에 대해 특정 날짜 이후의 Event Replay하여 최종 상태 저장
    @GetMapping("/replay/{processingGroup}/{startDateTime}")
    @Operation(summary = "지정한 Processing Group의 Event Handler를 지정한 시간 이후 Event만 Replay하면서 수행함")
    @Parameters({
            @Parameter(name = "processingGroup", in= ParameterIn.PATH, description = "Processing Groupname",
                    required = true, allowEmptyValue = false, example="elephant"),
            @Parameter(name = "startDateTime", in= ParameterIn.PATH, description = "시작일시(생략 시 모든 Event Replay)",
                    required = false, allowEmptyValue = true, example = "2024-01-01T00:00:00.00Z")
    })
    String replayEventFor(
            @PathVariable(name = "processingGroup") String processingGroupName,
            @PathVariable(name = "startDateTime") String startDateTime) {

        String startAt = ("".equals(startDateTime) ? "2000-01-01T00:00:00.00Z" : startDateTime);
        log.info("Executing replayEventsFor({}) from {}", processingGroupName, startAt);

        TrackingEventProcessorConfiguration tepConfig = TrackingEventProcessorConfiguration
                .forSingleThreadedProcessing()
                .andInitialTrackingToken(streamableMessageSource ->
                        streamableMessageSource.createTokenAt(Instant.parse(startAt)));

        configuration.eventProcessingConfiguration()
                .eventProcessorByProcessingGroup(processingGroupName, StreamingEventProcessor.class)
                .ifPresentOrElse(streamingEventProcessor ->
                {
                    if(streamingEventProcessor.supportsReset()) {
                        streamingEventProcessor.shutDown();
                        //streamingEventProcessor.resetTokens();    //모든 Event replay 시
                        streamingEventProcessor.resetTokens(tepConfig.getInitialTrackingToken());
                        streamingEventProcessor.start();
                        log.info("===== Start replay events for <{}> from {}", processingGroupName, startAt);
                    } else {
                        log.info("Find <{}>. But, It's not support replay", processingGroupName);
                    }
                }, () -> log.info("Can't find <{}>", processingGroupName));

        return "Replay Events is processed";
    }

    //-- Event Replay를 허용하는 모든 Event Handler에 대해 전체 Event를 Replay하여 최종 상태 저장
    @GetMapping("/replayAll")
    @Operation(summary = "Replay가 가능한 모든 Event Handler를 찾아 Event를 처음부터 Replay하도록 함")
    String replayEventsAll() {
        log.info("Executing replayEventsAll");
        List<StreamingEventProcessor> streamingEventProcessors = configuration.eventProcessingConfiguration()
                .eventProcessors().values().stream()
                .filter(StreamingEventProcessor.class::isInstance)
                .map(StreamingEventProcessor.class::cast)
                .toList();

        streamingEventProcessors.forEach(streamingEventProcessor -> {
            if(streamingEventProcessor.supportsReset()) {
                log.info("Trigger ResetTriggeredEvent for streamingEventProcessor <{}>", streamingEventProcessor);
                streamingEventProcessor.shutDown();
                streamingEventProcessor.resetTokens();
                streamingEventProcessor.start();
            }
        });

        return "Replay Events All is Processed";
    }
}