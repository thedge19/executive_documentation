package com.executive_documentation.subobjects.controller;

import com.executive_documentation.subobjects.dto.SubObjectRequestDto;
import com.executive_documentation.subobjects.dto.SubObjectResponseDto;
import com.executive_documentation.subobjects.model.SubObject;
import com.executive_documentation.subobjects.service.SubObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/subobjects")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "http://localhost:5173"})
public class SubObjectController {

    private final SubObjectService subObjectService;

    @GetMapping
    public ResponseEntity<List<SubObjectResponseDto>> getAll() {
        return ResponseEntity.ok(subObjectService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<SubObjectResponseDto>> getAllByProjectId(@PathVariable long id) {
        return ResponseEntity.ok(subObjectService.getAllByProjectId(id));
    }

    @GetMapping("/subObject/{subObjectId}")
    public ResponseEntity<SubObjectResponseDto> getBySubObjectId(@PathVariable long subObjectId) {
        return ResponseEntity.ok(subObjectService.getSubObject(subObjectId));
    }

    @PostMapping
    public SubObject create(
            @RequestBody SubObjectRequestDto dto) {
        log.info("Create SubObject: {}", dto.getName());
        SubObject subObjectCreated = subObjectService.create(dto);
        log.info("Create SubObject: {}", subObjectCreated);
        return subObjectCreated;
    }

    @PatchMapping("/{id}")
    public SubObject update(@PathVariable long id,
                            @RequestBody SubObject subObject) {
        log.info("Update SubObject: {}", subObject.getName());
        SubObject subObjectUpdated = subObjectService.update(id, subObject);
        log.info("Update SubObject: {}", subObjectUpdated);
        return subObjectUpdated;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Delete SubObject: {}", id);
        subObjectService.delete(id);
        log.info("SubObject with id: {} deleted", id);
    }
}
