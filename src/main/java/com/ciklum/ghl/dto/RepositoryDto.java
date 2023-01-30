package com.ciklum.ghl.dto;

import lombok.Data;

import java.util.List;

@Data
public class RepositoryDto {

    private String repositoryName;
    private String ownerLogin;

    private List<BranchDto> branches;
}
