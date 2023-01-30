package com.ciklum.ghl.services.github;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "github")
@Data
public class GitHubConfiguration {

    private String token;

    private Long cacheTimeout;
}
