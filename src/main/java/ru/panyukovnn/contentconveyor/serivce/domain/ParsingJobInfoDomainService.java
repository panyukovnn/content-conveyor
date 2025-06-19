package ru.panyukovnn.contentconveyor.serivce.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.panyukovnn.contentconveyor.model.parsingjobinfo.ParsingFrequency;
import ru.panyukovnn.contentconveyor.model.parsingjobinfo.ParsingJobInfo;
import ru.panyukovnn.contentconveyor.repository.ParsingJobInfoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParsingJobInfoDomainService {

    private final ParsingJobInfoRepository parsingJobInfoRepository;

    public List<ParsingJobInfo> findDailyParsingJobs() {
        return parsingJobInfoRepository.findByFrequency(ParsingFrequency.DAILY);
    }
}
