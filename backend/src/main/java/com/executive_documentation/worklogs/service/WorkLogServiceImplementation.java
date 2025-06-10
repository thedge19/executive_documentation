package com.executive_documentation.worklogs.service;

import com.executive_documentation.acts.dto.ActLogResponseDto;
import com.executive_documentation.acts.dto.ActMapper;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.repository.ActRepository;
import com.executive_documentation.subobjects.service.SubObjectService;
import com.executive_documentation.worklogs.dto.WorkLogDto;
import com.executive_documentation.worklogs.dto.WorkLogMapper;
import com.executive_documentation.worklogs.model.WorkLog;
import com.executive_documentation.worklogs.repository.WorkLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkLogServiceImplementation implements WorkLogService {

    private final ActRepository actRepository;
    private final WorkLogRepository workLogRepository;
    private final SubObjectService subObjectService;
    private final WorkLogMapper workLogMapper;
    private final ActMapper actMapper;

    @Override
    public List<WorkLogDto> getWorkLog3() {
        List<WorkLog> workLogs = workLogRepository.findAllByOrderByWorkLogNumber();

        return workLogs.stream().map(workLogMapper::logToLogDto).toList();
    }

    @Override
    public List<ActLogResponseDto> getWorkLog6() {
        List<Act> acts = actRepository.findAllByOrderByEndDateAscActNumberAsc();

        return acts.stream().map(ActMapper::actToActLogResponseDto).toList();
    }

    @Transactional
    @Override
    public void fillInTheLog3() {

        workLogRepository.deleteAll();

        List<Act> acts = actRepository.findAllByOrderByStartDateAscActNumberAsc();
        List<LocalDate> workLogDates = new ArrayList<>();
        List<Long> subObjectIds = new ArrayList<>();
        Map<LocalDate, Map<Long, List<String>>> logRows = new HashMap<>();

        for (Act act : acts) {
            ArrayList<LocalDate> actDates = new ArrayList<>();
            for (LocalDate d = act.getStartDate(); !d.isAfter(act.getEndDate()); d = d.plusDays(1)) {
                actDates.add(d);
                if (!workLogDates.contains(d)) {
                    workLogDates.add(d);
                }
            }

            Long subObjectId = act.getSubObject().getId();
            subObjectIds.add(subObjectId);
            double workDone = act.getWorkDone().doubleValue();

            if (!subObjectIds.contains(subObjectId)) {
                subObjectIds.add(subObjectId);
            }

            String currentWork = act.getWorks().split(":", 2)[1];

            String[] works = currentWork.split(" ");
            int worksSize = works.length;

            StringBuilder workString = new StringBuilder();
            String units = works[worksSize - 1];

            for (int j = 0; j < worksSize - 3; j++) {
                workString.append(works[j]);
                workString.append(" ");
            }

            currentWork = workString.toString();

            double rowWorkDone = workDone / actDates.size();

            for (int k = 0; k < actDates.size(); k++) {
                LocalDate rowDate = actDates.get(k);

                if (actDates.size() > 1) {
                    if (k != actDates.size() - 1) {
                        workDone = workDone - rowWorkDone;
                    } else {
                        rowWorkDone = workDone;
                    }
                }
                String rowCurrentWork = currentWork + " - " + String.format("%.2f", rowWorkDone) + " " + units;

                if (logRows.get(rowDate) == null) {
                    Map<Long, List<String>> row = new HashMap<>();
                    List<String> rowWorks = new ArrayList<>();
                    rowWorks.add(rowCurrentWork);
                    row.put(subObjectId, rowWorks);

                    logRows.put(actDates.get(k), row);
                } else {
                    if (logRows.get(rowDate).get(subObjectId) == null) {
                        List<String> rowWorks = new ArrayList<>();
                        rowWorks.add(rowCurrentWork);
                        logRows.get(rowDate).put(subObjectId, rowWorks);
                    } else {
                        logRows.get(rowDate).get(subObjectId).add(rowCurrentWork);
                    }
                }
            }
        }

        int i = 1;

        for (LocalDate d : workLogDates) {

            WorkLog workLog = new WorkLog();

            workLog.setWorkDate(d);
            Set<Long> rowKeySet = logRows.get(d).keySet();
            StringBuilder workLogName = new StringBuilder();

            for (Long rowKey : rowKeySet) {
                workLogName.append(subObjectService.get(rowKey).getName()).append(": ");
                for (String work : logRows.get(d).get(rowKey)) {
                    workLogName.append(work).append("; ");
                }
            }
            workLog.setName(workLogName.toString());
            workLog.setWorkLogNumber(i);
            workLogRepository.save(workLog);

            i++;
        }
    }

    private int lastRow(Sheet sheet) {
        int rowNumber = 4;

        while (!Objects.equals(sheet.getRow(rowNumber).getCell(6), null)) {
            rowNumber++;
        }
        return rowNumber;
    }
}
