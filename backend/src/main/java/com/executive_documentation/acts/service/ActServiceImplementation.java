package com.executive_documentation.acts.service;

import com.executive_documentation.acts.dto.ActMapper;
import com.executive_documentation.acts.dto.ActResponseDto;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.repository.ActRepository;
import com.executive_documentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActServiceImplementation  implements ActService {
    private final ActMapper actMapper;
    private final ActRepository actRepository;

    @Override
    public ActResponseDto get(Long id) {
        return actMapper.ActToActResponseDto(findActOrNot(id));
    }

    @Override
    public List<ActResponseDto> getAll() {
        List<Act> acts = actRepository.findAllByOrderByEndDateAscActNumberAsc();
        return acts.stream().map(actMapper::ActToActResponseDto).toList();
    }

    public Act findActOrNot(long id) {
        return actRepository.findById(id).orElseThrow(() -> new NotFoundException("Акт найден"));
    }
}
