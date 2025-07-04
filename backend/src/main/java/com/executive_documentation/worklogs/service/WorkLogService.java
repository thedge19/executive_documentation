package com.executive_documentation.worklogs.service;

import com.executive_documentation.acts.dto.act.ActLogResponseDto;
import com.executive_documentation.worklogs.dto.WorkLogDto;

import java.util.List;

public interface WorkLogService {

    List<WorkLogDto> getWorkLog3();

    List<ActLogResponseDto> getWorkLog6();

    void fillInTheLog3();
}
