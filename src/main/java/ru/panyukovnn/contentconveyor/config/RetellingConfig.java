package ru.panyukovnn.contentconveyor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEventType;
import ru.panyukovnn.contentconveyor.property.ExecutorsProperty;
import ru.panyukovnn.contentconveyor.serivce.articleparser.ArticleParser;
import ru.panyukovnn.contentconveyor.serivce.eventprocessor.EventProcessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@EnableAsync
@Configuration
public class RetellingConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService tgListenerExecutor(ExecutorsProperty executorsProperty) {
        return new ThreadPoolExecutor(
            executorsProperty.getTgListener().getThreads(),
            executorsProperty.getTgListener().getThreads(),
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(executorsProperty.getTgListener().getQueueCapacity()),
            new ThreadPoolExecutor.AbortPolicy());
    }

    @Bean
    public Map<ProcessingEventType, EventProcessor> eventProcessorByType(List<EventProcessor> eventProcessors) {
        return eventProcessors.stream()
            .collect(Collectors.toMap(EventProcessor::getProcessingEventType, Function.identity()));
    }

    @Bean
    public Map<Source, ArticleParser> articleParserBySource(List<ArticleParser> articleParsers) {
        return articleParsers.stream()
            .collect(Collectors.toMap(ArticleParser::getSource, Function.identity()));
    }
}
