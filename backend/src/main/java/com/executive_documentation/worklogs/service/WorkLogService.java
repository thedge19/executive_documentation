package com.executive_documentation.worklogs.service;

import com.executive_documentation.acts.dto.ActLogResponseDto;
import com.executive_documentation.worklogs.dto.WorkLogDto;

import java.io.IOException;
import java.util.List;

public interface WorkLogService {

    List<WorkLogDto> getWorkLog3();

    List<ActLogResponseDto> getWorkLog6();

    void fillInTheLog3();
}
