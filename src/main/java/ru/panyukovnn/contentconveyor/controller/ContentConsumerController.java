package ru.panyukovnn.contentconveyor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.panyukovnn.contentconveyor.dto.ConsumeContentRequest;
import ru.panyukovnn.contentconveyor.dto.common.CommonRequest;
import ru.panyukovnn.contentconveyor.dto.common.CommonResponse;
import ru.panyukovnn.contentconveyor.serivce.ContentConsumerHandler;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentConsumerController {

    private final ContentConsumerHandler contentConsumerHandler;

    @PostMapping("/consume")
    public CommonResponse<Void> postConsume(@RequestBody @Valid CommonRequest<ConsumeContentRequest> request) {
        contentConsumerHandler.handleConsumeContent(request.getBody());

        return CommonResponse.<Void>builder().build();
    }
}
