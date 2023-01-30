package com.ciklum.ghl;

import com.ciklum.ghl.dto.DtoConverter;
import com.ciklum.ghl.dto.Error;
import com.ciklum.ghl.dto.RepositoryDto;
import com.ciklum.ghl.services.github.GitHubCache;
import com.ciklum.ghl.services.github.GitHubService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
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

    @GetMapping(value = "/users/{user}")
    public ResponseEntity<List<RepositoryDto>> getUser(@PathVariable(value = "user") String user) {
        List<RepositoryDto> result = cache.get(user);
        if (result == null) {
            result = gitHubService
                    .getRepositories(user)
                    .stream()
                    .map(DtoConverter::convert)
                    .toList();
            cache.set(user, result);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
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

    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<Error> handleHttpClientException(HttpServletRequest req, HttpClientErrorException ex) {
        return new ResponseEntity<>(new Error(ex.getStatusCode().value(), ex.getMessage()),
                HttpStatus.valueOf(ex.getStatusCode().value()));
    }
}
