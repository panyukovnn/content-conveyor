package ru.panyukovnn.contentconveyor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.panyukovnn.contentconveyor.client.OpenAiClient;
import ru.panyukovnn.contentconveyor.repository.ContentRepository;
import ru.panyukovnn.contentconveyor.repository.ProcessingEventRepository;
import ru.panyukovnn.contentconveyor.repository.PromptRepository;
import ru.panyukovnn.contentconveyor.repository.PublishingChannelRepository;
import ru.panyukovnn.contentconveyor.serivce.autodatafinder.impl.HabrDataFinder;
import ru.panyukovnn.contentconveyor.serivce.loader.impl.HabrLoader;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
public abstract class AbstractTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected HabrLoader habrLoader;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected HabrDataFinder habrDataFinder;
    @Autowired
    protected PromptRepository promptRepository;
    @Autowired
    protected ContentRepository contentRepository;
    @Autowired
    protected ProcessingEventRepository processingEventRepository;
    @Autowired
    protected PublishingChannelRepository publishingChannelRepository;

    @MockBean
    protected OpenAiClient openAiClient;
}