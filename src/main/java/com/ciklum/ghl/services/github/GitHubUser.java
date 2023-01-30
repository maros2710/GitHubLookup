package com.ciklum.ghl.services.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubUser {
    private String name;

    private String login;
    @JsonProperty("public_repos")
    private Integer publicRepos;
}
