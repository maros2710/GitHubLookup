package com.ciklum.ghl.services.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubBranch {

    private String name;

    private GitHubCommit commit;
}
