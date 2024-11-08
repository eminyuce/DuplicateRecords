package com.acqu.co.excel.converter.actuator.controller;

import com.acqu.co.excel.converter.actuator.exception.ServiceStatus;
import com.acqu.co.excel.converter.actuator.model.AcquUserEntity;
import com.acqu.co.excel.converter.actuator.service.AcquUserEntityService;
import com.acqu.co.excel.converter.actuator.util.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.acqu.co.excel.converter.actuator.util.Constants.*;

@RestController
@RequestMapping("/api/acqu-users")
@Slf4j
public class AcquUserEntityController {

    private final AcquUserEntityService acquUserEntityService;

    @Autowired
    public AcquUserEntityController(AcquUserEntityService acquUserEntityService) {
        this.acquUserEntityService = acquUserEntityService;
    }

    @GetMapping
    public ResponseEntity<List<AcquUserEntity>> getAllAcquUsers(@RequestParam(required = false) String sort) {
        Sort sorting = Sort.by(sort != null ? sort : "userEntityId");
        List<AcquUserEntity> users = acquUserEntityService.findAll(sorting);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/export")
    public ResponseEntity<?>  exportAcquUserEntities() {
        try {
            InputStreamResource resource = acquUserEntityService.exportXls();
            HttpHeaders headers = new HttpHeaders();

            String fileName = "AcquUserEntities_" + DateUtil.getFormattedDateStr() + ".xlsx";

            headers.add(CONTENT_DISPOSITION, ATTACHMENT_FILENAME + fileName);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (IOException e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ServiceStatus(
                    "Unexpected error occurred",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e.getMessage()
            ));
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload an Excel file with AcquUserEntity data",
            description = "Upload a file containing user data in Excel format.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    public ResponseEntity<?> uploadAcquUserEntityFromExcel(
            @Parameter(description = "Excel file to upload", required = true)
            @RequestPart("file") MultipartFile file, @RequestHeader HttpHeaders requestHeader) {
        try {
            List<AcquUserEntity> users = acquUserEntityService.uploadAcquUserEntityFromExcel(file);
            return new ResponseEntity<>(users, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ServiceStatus(
                    "Unexpected error occurred",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e.getMessage()
            ));
        }
    }

    @GetMapping("/paging")
    public ResponseEntity<Page<AcquUserEntity>> getPageAcquUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "userEntityId", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<AcquUserEntity> users = acquUserEntityService.findAll(search, pageable);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

}
