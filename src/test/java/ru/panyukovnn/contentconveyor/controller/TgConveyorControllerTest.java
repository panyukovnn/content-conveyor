package ru.panyukovnn.contentconveyor.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.panyukovnn.contentconveyor.AbstractTest;
import ru.panyukovnn.contentconveyor.dto.TgConveyorRequest;
import ru.panyukovnn.contentconveyor.dto.chathistory.ChatHistoryResponse;
import ru.panyukovnn.contentconveyor.dto.chathistory.MessageDto;
import ru.panyukovnn.contentconveyor.dto.chathistory.MessagesBatch;
import ru.panyukovnn.contentconveyor.dto.common.CommonRequest;
import ru.panyukovnn.contentconveyor.model.ConveyorType;
import ru.panyukovnn.contentconveyor.model.Prompt;
import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.model.content.ContentType;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEvent;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEventType;
import ru.panyukovnn.contentconveyor.model.publishingchannels.PublishingChannel;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TgConveyorControllerTest extends AbstractTest {

    @Disabled
    @Test
    @Transactional
    @Sql(scripts = {
        "/db/controller/tgconveyor/truncate_contents.sql",
        "/db/controller/tgconveyor/truncate_processing_events.sql",
        "/db/controller/tgconveyor/truncate_prompts.sql",
        "/db/controller/tgconveyor/truncate_publishing_channels.sql"
    })
    void when_processChat_then_success() throws Exception {
        // Arrange
        TgConveyorRequest request = new TgConveyorRequest();
        request.setPublicChatName("test_chat");
        request.setChatId(123L);
        request.setTopicName("test_topic");
        request.setLimit(10);
        request.setDateFrom(LocalDateTime.now().minusDays(1));
        request.setDateTo(LocalDateTime.now());
        request.setMapPrompt("Test map prompt");
        request.setReducePrompt("Test reduce prompt");
        request.setPublishingChannelExternalId("test_channel");

        CommonRequest<TgConveyorRequest> commonRequest = new CommonRequest<>();
        commonRequest.setBody(request);

        MessagesBatch messageBatch = MessagesBatch.builder()
            .count(1)
            .messages(List.of(
                MessageDto.builder()
                    .id(1234L)
                    .senderId(12345L)
                    .replyToText(null)
                    .text("test message")
                    .build()
            ))
            .build();
        ChatHistoryResponse chatHistoryResponse = ChatHistoryResponse.builder()
            .chatId(123L)
            .chatTitle("Test Chat")
            .topicName("Test Topic")
            .firstMessageDateTime(LocalDateTime.now().minusDays(1))
            .lastMessageDateTime(LocalDateTime.now())
            .messageBatches(List.of(
                messageBatch))
            .build();

        // Mock tg-chats-collector response
        WireMock.stubFor(get(urlPathEqualTo("/tg-chats-collector/api/v1/getChatHistory"))
            .withQueryParam("publicChatName", equalTo("test_chat"))
            .withQueryParam("topicNamePart", equalTo("test_topic"))
            .withQueryParam("limit", equalTo("10"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(chatHistoryResponse))));

        // Act & Assert
        mockMvc.perform(post("/api/v1/conveyor/tg/processChat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commonRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNotEmpty());

        // Verify wiremock interaction
        WireMock.verify(getRequestedFor(urlPathEqualTo("/tg-chats-collector/api/v1/getChatHistory"))
            .withQueryParam("publicChatName", equalTo("test_chat"))
            .withQueryParam("topicNamePart", equalTo("test_topic"))
            .withQueryParam("limit", equalTo("10")));

        List<Prompt> prompts = promptRepository.findAll();
        assertThat(prompts).hasSize(1);
        Prompt prompt = prompts.get(0);
        assertAll(
            () -> assertEquals(request.getMapPrompt(), prompt.getMapPrompt()),
            () -> assertEquals(request.getReducePrompt(), prompt.getReducePrompt())
        );

        List<PublishingChannel> publishingChannels = publishingChannelRepository.findAll();
        assertThat(publishingChannels).hasSize(1);
        PublishingChannel publishingChannel = publishingChannels.get(0);
        assertAll(
            () -> assertEquals(request.getPublishingChannelExternalId(), publishingChannel.getExternalId()),
            () -> assertEquals(request.getChatId(), publishingChannel.getChatId()),
            () -> assertEquals(request.getTopicId(), publishingChannel.getTopicId())
        );

        List<Content> contents = contentRepository.findAll();
        assertThat(contents).hasSize(1);
        Content content = contents.get(0);
        assertAll(
            () -> assertEquals(chatHistoryResponse.getChatId().toString(), content.getLink()),
            () -> assertEquals(ContentType.TG_MESSAGE_BATCH, content.getType()),
            () -> assertEquals(Source.TG, content.getSource()),
            () -> assertEquals(chatHistoryResponse.getChatTitle() + "/" + chatHistoryResponse.getTopicName(), content.getTitle()),
            () -> assertNull(content.getMeta()),
            () -> assertEquals(chatHistoryResponse.getFirstMessageDateTime(), content.getPublicationDate()),
            () -> assertEquals(objectMapper.writeValueAsString(messageBatch.getMessages()), content.getContent()),
            () -> assertNotNull(content.getParentBatchId()),
            () -> assertNotNull(content.getChildBatchId())
        );

        List<ProcessingEvent> processingEvents = processingEventRepository.findAll();
        assertThat(processingEvents)
            .hasSize(1)
            .allSatisfy(processingEvent -> {
                assertEquals(ProcessingEventType.MAP, processingEvent.getType());
                assertEquals(ConveyorType.MAP_REDUCE, processingEvent.getConveyorType());
                assertNull(processingEvent.getContentId());
                assertEquals(content.getParentBatchId(), processingEvent.getContentBatchId());
                assertEquals(prompt.getId(), processingEvent.getPromptId());
//                assertEquals(publishingChannel.getId(), processingEvent.getPublishingChannelId()); // TODO
            });
    }
} 