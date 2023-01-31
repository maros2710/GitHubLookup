package com.ciklum.ghl.services.github;

import com.ciklum.ghl.services.github.dto.GitHubBranch;
import com.ciklum.ghl.services.github.dto.GitHubOwner;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRepository {

    private GitHubOwner owner;
    private String name;
    private Boolean fork;

    @JsonProperty("branches_url")
    private String branchesUrl;

    private List<GitHubBranch> branches;

    @JsonIgnore
    private int branchesCount;

}
