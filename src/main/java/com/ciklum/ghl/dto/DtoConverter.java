package com.ciklum.ghl.dto;

import com.ciklum.ghl.services.github.GitHubRepository;

public class DtoConverter {

    public static RepositoryDto convert(GitHubRepository repository) {
        RepositoryDto dto = new RepositoryDto();
        dto.setOwnerLogin(repository.getOwner().getLogin());
        dto.setRepositoryName(repository.getName());
        dto.setBranches(repository.getBranches().stream().map(it -> {
            BranchDto branchDto = new BranchDto();
            branchDto.setName(it.getName());
            branchDto.setLastCommitSha(it.getCommit().getSha());
            return branchDto;
        }).toList());
        return dto;
    }

}
