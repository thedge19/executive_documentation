package com.executive_documentation.acts.dto;

import com.executive_documentation.acts.model.Act;
import org.mapstruct.Mapper;

import java.time.format.DateTimeFormatter;


@Mapper(componentModel = "spring")
public interface ActMapper {

    default ActResponseDto actToActResponseDto(Act act) {
        if (act == null) {
            return null;
        }

        return ActResponseDto.builder()
                .id(act.getId())
                .projectName(act.getProject().getName())
                .works(act.getWorks())
                .actNumber(act.getActNumber())
                .startDate(act.getStartDate() != null
                        ? act.getStartDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        : null)
                .endDate(act.getEndDate() != null
                        ? act.getEndDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        : null)
                .materials(act.getMaterials())
                .submittedDocuments(act.getSubmittedDocuments())
                .inAccordWith(act.getInAccordWith())
                .nextWorks(act.getNextWorks())
                .inRegistry(act.getInRegistry())
                .build();
    }

    default ActLogResponseDto actToActLogResponseDto(Act act) {
        if (act == null) {
            return null;
        }

        return ActLogResponseDto.builder()
                .id(act.getId())
                .actNumber(act.getActNumber())
                .works(act.getWorks())
                .endDate(act.getEndDate() != null
                        ? act.getEndDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        : null)
                .build();
    }
}

