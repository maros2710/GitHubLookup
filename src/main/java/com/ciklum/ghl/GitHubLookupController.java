package com.ciklum.ghl;

import com.ciklum.ghl.dto.DtoConverter;
import com.ciklum.ghl.dto.Error;
import com.ciklum.ghl.dto.RepositoryDto;
import com.ciklum.ghl.services.github.GitHubCache;
import com.ciklum.ghl.services.github.GitHubService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
@Slf4j
public class GitHubLookupController {

    private final GitHubService gitHubService;

    private final GitHubCache cache;

    public GitHubLookupController(GitHubService gitHubService, GitHubCache cache) {
        this.gitHubService = gitHubService;
        this.cache = cache;
    }

    @GetMapping(value = "/user/{user}")
    public ResponseEntity user(@PathVariable(value = "user") String user) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("Missing user parameter");
            }

            List<RepositoryDto> result = cache.get(user);
            if (result == null) {
                result = gitHubService.getRepositories(user).stream().map(DtoConverter::convert).toList();
                cache.set(user, result);
            }
            return new ResponseEntity(result, HttpStatus.OK);
        } catch (HttpClientErrorException ex) {
            log.error(String.format("User %s", user), ex);
            return new ResponseEntity(new Error(ex.getStatusCode().value(), ex.getMessage()),
                    HttpStatus.valueOf(ex.getStatusCode().value()));
        }
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public String handleHttpMediaTypeNotAcceptableException() throws JsonProcessingException {
        Error error =
                new Error(HttpStatus.NOT_ACCEPTABLE.value(), "Only " + MediaType.APPLICATION_JSON + " is supported");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(error);
    }
}
