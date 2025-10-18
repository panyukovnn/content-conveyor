package ru.panyukovnn.contentconveyor.serivce.eventprocessor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.panyukovnn.contentconveyor.exception.InvalidProcessingEventException;
import ru.panyukovnn.contentconveyor.model.ConveyorType;
import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEvent;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEventType;
import ru.panyukovnn.contentconveyor.model.publishingchannels.PublishingChannel;
import ru.panyukovnn.contentconveyor.property.HardcodedPublishingProperties;
import ru.panyukovnn.contentconveyor.serivce.domain.ContentDomainService;
import ru.panyukovnn.contentconveyor.serivce.domain.ProcessingEventDomainService;
import ru.panyukovnn.contentconveyor.serivce.domain.PublishingChannelDomainService;
import ru.panyukovnn.contentconveyor.serivce.telegram.TgSender;
import ru.panyukovnn.contentconveyor.util.JsonUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublishingEventProcessorImpl: Тест обработки событий публикации")
class PublishingEventProcessorImplTest {

    @Mock
    private JsonUtil jsonUtil;

    @Mock
    private TgSender tgSender;

    @Mock
    private ContentDomainService contentDomainService;

    @Mock
    private ProcessingEventDomainService processingEventDomainService;

    @Mock
    private HardcodedPublishingProperties hardcodedPublishingProperties;

    @Mock
    private PublishingChannelDomainService publishingChannelDomainService;

    @InjectMocks
    private PublishingEventProcessorImpl publishingEventProcessor;

    private UUID contentId;
    private UUID publishingChannelSetsId;
    private ProcessingEvent processingEvent;
    private Content content;
    private PublishingChannel publishingChannel1;
    private PublishingChannel publishingChannel2;

    @BeforeEach
    void setUp() {
        contentId = UUID.randomUUID();
        publishingChannelSetsId = UUID.randomUUID();

        // Настройка тестового контента
        content = Content.builder()
            .id(contentId)
            .title("Test Article Title")
            .link("https://test.com/article")
            .source(Source.JAVA_HABR)
            .content("This is a test article content that needs to be published.")
            .build();

        // Настройка каналов публикации
        publishingChannel1 = PublishingChannel.builder()
            .id(UUID.randomUUID())
            .name("Channel 1")
            .chatId(-1001234567890L)
            .topicId(100L)
            .publishingChannelSetsId(publishingChannelSetsId)
            .build();

        publishingChannel2 = PublishingChannel.builder()
            .id(UUID.randomUUID())
            .name("Channel 2")
            .chatId(-1009876543210L)
            .topicId(200L)
            .publishingChannelSetsId(publishingChannelSetsId)
            .build();

        // Настройка события обработки
        processingEvent = ProcessingEvent.builder()
            .id(UUID.randomUUID())
            .contentId(contentId)
            .type(ProcessingEventType.PUBLISHING)
            .conveyorType(ConveyorType.JUST_RETELLING)
            .publishingChannelSetsId(publishingChannelSetsId)
            .build();
    }

    @Test
    @DisplayName("Успешная публикация контента в несколько каналов с использованием publishingChannelSetsId")
    void testSuccessfulPublishingToMultipleChannelsWithChannelSet() {
        // Arrange: подготовка моков
        when(contentDomainService.findById(contentId)).thenReturn(Optional.of(content));
        when(publishingChannelDomainService.findByPublishingChannelSet(publishingChannelSetsId))
            .thenReturn(List.of(publishingChannel1, publishingChannel2));
        when(jsonUtil.toJson(any())).thenReturn("{\"id\":\"test-event\"}");

        // Act: выполнение тестируемого метода
        publishingEventProcessor.process(processingEvent);

        // Assert: проверка результатов

        // 1. Проверяем, что контент был найден
        verify(contentDomainService, times(1)).findById(contentId);

        // 2. Проверяем, что каналы публикации были получены по publishingChannelSetsId
        verify(publishingChannelDomainService, times(1))
            .findByPublishingChannelSet(publishingChannelSetsId);

        // 3. Проверяем, что сообщения были отправлены в оба канала с правильными параметрами
        ArgumentCaptor<Long> chatIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> topicIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(tgSender, times(2)).sendMessage(
            chatIdCaptor.capture(),
            topicIdCaptor.capture(),
            messageCaptor.capture()
        );

        List<Long> capturedChatIds = chatIdCaptor.getAllValues();
        List<Long> capturedTopicIds = topicIdCaptor.getAllValues();
        List<String> capturedMessages = messageCaptor.getAllValues();

        // Проверяем отправку в первый канал
        assertEquals(-1001234567890L, capturedChatIds.get(0));
        assertEquals(100L, capturedTopicIds.get(0));

        // Проверяем отправку во второй канал
        assertEquals(-1009876543210L, capturedChatIds.get(1));
        assertEquals(200L, capturedTopicIds.get(1));

        // Проверяем формат сообщения (должен содержать заголовок, ссылку и контент)
        String expectedMessageStart = "Test Article Title\nhttps://test.com/article\n\n";
        assertTrue(capturedMessages.get(0).startsWith(expectedMessageStart),
            "Сообщение должно начинаться с заголовка и ссылки");
        assertTrue(capturedMessages.get(0).contains(content.getContent()),
            "Сообщение должно содержать контент статьи");

        // 4. Проверяем, что событие было обновлено на PUBLISHED
        assertEquals(ProcessingEventType.PUBLISHED, processingEvent.getType());

        // 5. Проверяем, что событие было сохранено
        verify(processingEventDomainService, times(1)).save(processingEvent);

        // 6. Проверяем, что JSON-сериализация была вызвана для логирования
        verify(jsonUtil, times(2)).toJson(processingEvent);
    }

    @Test
    @DisplayName("Успешная публикация с использованием hardcoded свойств для JAVA_DZONE")
    void testSuccessfulPublishingWithHardcodedPropertiesForDzone() {
        // Arrange: настройка для источника JAVA_DZONE без publishingChannelSetsId
        content.setSource(Source.JAVA_DZONE);
        processingEvent.setPublishingChannelSetsId(null);

        when(contentDomainService.findById(contentId)).thenReturn(Optional.of(content));
        when(hardcodedPublishingProperties.getChatId()).thenReturn(-1001111111111L);
        when(hardcodedPublishingProperties.getJavaDzoneTopicId()).thenReturn(300L);
        when(jsonUtil.toJson(any())).thenReturn("{\"id\":\"test-event\"}");

        // Act
        publishingEventProcessor.process(processingEvent);

        // Assert
        verify(tgSender, times(1)).sendMessage(
            eq(-1001111111111L),
            eq(300L),
            anyString()
        );

        assertEquals(ProcessingEventType.PUBLISHED, processingEvent.getType());
        verify(processingEventDomainService, times(1)).save(processingEvent);
    }

    @Test
    @DisplayName("Успешная публикация с использованием hardcoded свойств для JAVA_MEDIUM")
    void testSuccessfulPublishingWithHardcodedPropertiesForMedium() {
        // Arrange
        content.setSource(Source.JAVA_MEDIUM);
        processingEvent.setPublishingChannelSetsId(null);

        when(contentDomainService.findById(contentId)).thenReturn(Optional.of(content));
        when(hardcodedPublishingProperties.getChatId()).thenReturn(-1001111111111L);
        when(hardcodedPublishingProperties.getJavaMediumTopicId()).thenReturn(400L);
        when(jsonUtil.toJson(any())).thenReturn("{\"id\":\"test-event\"}");

        // Act
        publishingEventProcessor.process(processingEvent);

        // Assert
        verify(tgSender, times(1)).sendMessage(
            eq(-1001111111111L),
            eq(400L),
            anyString()
        );

        assertEquals(ProcessingEventType.PUBLISHED, processingEvent.getType());
    }

    @Test
    @DisplayName("Успешная публикация с использованием hardcoded свойств для TG")
    void testSuccessfulPublishingWithHardcodedPropertiesForTg() {
        // Arrange
        content.setSource(Source.TG);
        processingEvent.setPublishingChannelSetsId(null);

        when(contentDomainService.findById(contentId)).thenReturn(Optional.of(content));
        when(hardcodedPublishingProperties.getChatId()).thenReturn(-1001111111111L);
        when(hardcodedPublishingProperties.getTgMessageBatchTopicId()).thenReturn(500L);
        when(jsonUtil.toJson(any())).thenReturn("{\"id\":\"test-event\"}");

        // Act
        publishingEventProcessor.process(processingEvent);

        // Assert
        verify(tgSender, times(1)).sendMessage(
            eq(-1001111111111L),
            eq(500L),
            anyString()
        );

        assertEquals(ProcessingEventType.PUBLISHED, processingEvent.getType());
    }

    @Test
    @DisplayName("Исключение при отсутствии контента")
    void testThrowsExceptionWhenContentNotFound() {
        // Arrange
        when(contentDomainService.findById(contentId)).thenReturn(Optional.empty());

        // Act & Assert
        InvalidProcessingEventException exception = assertThrows(
            InvalidProcessingEventException.class,
            () -> publishingEventProcessor.process(processingEvent)
        );

        assertEquals("42d6", exception.getId());
        assertEquals("Не удалось найти контент", exception.getMessage());

        // Проверяем, что отправка не была выполнена
        verify(tgSender, never()).sendMessage(anyLong(), anyLong(), anyString());
        verify(processingEventDomainService, never()).save(any());
    }

    @Test
    @DisplayName("Исключение при пустом списке каналов публикации")
    void testThrowsExceptionWhenPublishingChannelsEmpty() {
        // Arrange
        when(contentDomainService.findById(contentId)).thenReturn(Optional.of(content));
        when(publishingChannelDomainService.findByPublishingChannelSet(publishingChannelSetsId))
            .thenReturn(List.of());

        // Act & Assert
        InvalidProcessingEventException exception = assertThrows(
            InvalidProcessingEventException.class,
            () -> publishingEventProcessor.process(processingEvent)
        );

        assertEquals("4df0", exception.getId());
        assertEquals("Не удалось найти данные о канале публикации", exception.getMessage());

        verify(tgSender, never()).sendMessage(anyLong(), anyLong(), anyString());
        verify(processingEventDomainService, never()).save(any());
    }

    @Test
    @DisplayName("Исключение для источника JAVA_HABR без publishingChannelSetsId")
    void testThrowsExceptionForHabrWithoutChannelSet() {
        // Arrange
        content.setSource(Source.JAVA_HABR);
        processingEvent.setPublishingChannelSetsId(null);

        when(contentDomainService.findById(contentId)).thenReturn(Optional.of(content));

        // Act & Assert
        assertThrows(
            Exception.class, // ConveyorException
            () -> publishingEventProcessor.process(processingEvent)
        );

        verify(tgSender, never()).sendMessage(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("Обработка ошибки при отправке сообщения в Telegram")
    void testHandlesExceptionDuringSending() {
        // Arrange
        when(contentDomainService.findById(contentId)).thenReturn(Optional.of(content));
        when(publishingChannelDomainService.findByPublishingChannelSet(publishingChannelSetsId))
            .thenReturn(List.of(publishingChannel1));

        // Симулируем ошибку при отправке
        doThrow(new RuntimeException("Telegram API error"))
            .when(tgSender).sendMessage(anyLong(), anyLong(), anyString());

        // Act
        publishingEventProcessor.process(processingEvent);

        // Assert: проверяем, что событие было помечено как PUBLICATION_ERROR
        assertEquals(ProcessingEventType.PUBLICATION_ERROR, processingEvent.getType());

        // Проверяем, что событие было сохранено несмотря на ошибку (в блоке finally)
        verify(processingEventDomainService, times(1)).save(processingEvent);
    }

    @Test
    @DisplayName("Форматирование сообщения без заголовка (fallback на 'Ссылка')")
    void testMessageFormattingWithoutTitle() {
        // Arrange
        content.setTitle(null);

        when(contentDomainService.findById(contentId)).thenReturn(Optional.of(content));
        when(publishingChannelDomainService.findByPublishingChannelSet(publishingChannelSetsId))
            .thenReturn(List.of(publishingChannel1));
        when(jsonUtil.toJson(any())).thenReturn("{\"id\":\"test-event\"}");

        // Act
        publishingEventProcessor.process(processingEvent);

        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(tgSender, times(1)).sendMessage(anyLong(), anyLong(), messageCaptor.capture());

        String capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage.startsWith("Ссылка\nhttps://test.com/article\n\n"),
            "При отсутствии заголовка должен использоваться текст 'Ссылка'");
    }

    @Test
    @DisplayName("Форматирование сообщения без ссылки")
    void testMessageFormattingWithoutLink() {
        // Arrange
        content.setLink(null);

        when(contentDomainService.findById(contentId)).thenReturn(Optional.of(content));
        when(publishingChannelDomainService.findByPublishingChannelSet(publishingChannelSetsId))
            .thenReturn(List.of(publishingChannel1));
        when(jsonUtil.toJson(any())).thenReturn("{\"id\":\"test-event\"}");

        // Act
        publishingEventProcessor.process(processingEvent);

        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(tgSender, times(1)).sendMessage(anyLong(), anyLong(), messageCaptor.capture());

        String capturedMessage = messageCaptor.getValue();
        assertEquals(content.getContent(), capturedMessage,
            "При отсутствии ссылки должен возвращаться только контент без заголовка");
    }

    @Test
    @DisplayName("Проверка типа события процессора")
    void testGetProcessingEventType() {
        // Act
        ProcessingEventType eventType = publishingEventProcessor.getProcessingEventType();

        // Assert
        assertEquals(ProcessingEventType.PUBLISHING, eventType);
    }

    @Test
    @DisplayName("Частичная публикация: ошибка после успешной отправки в первый канал")
    void testPartialPublishingWithErrorOnSecondChannel() {
        // Arrange
        when(contentDomainService.findById(contentId)).thenReturn(Optional.of(content));
        when(publishingChannelDomainService.findByPublishingChannelSet(publishingChannelSetsId))
            .thenReturn(List.of(publishingChannel1, publishingChannel2));
        when(jsonUtil.toJson(any())).thenReturn("{\"id\":\"test-event\"}");

        // Первая отправка успешна, вторая - с ошибкой
        doNothing()
            .doThrow(new RuntimeException("Network error on second channel"))
            .when(tgSender).sendMessage(anyLong(), anyLong(), anyString());

        // Act
        publishingEventProcessor.process(processingEvent);

        // Assert
        // Проверяем, что была попытка отправки в оба канала
        verify(tgSender, times(2)).sendMessage(anyLong(), anyLong(), anyString());

        // Событие должно быть помечено как PUBLICATION_ERROR из-за исключения
        assertEquals(ProcessingEventType.PUBLICATION_ERROR, processingEvent.getType());

        // Событие должно быть сохранено
        verify(processingEventDomainService, times(1)).save(processingEvent);
    }
}