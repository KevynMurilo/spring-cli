package com.springcli.service;

import com.springcli.client.SpringInitializrClient;
import com.springcli.model.SpringMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataService {

    private final SpringInitializrClient initializrClient;
    private final CacheService cacheService;

    public SpringMetadata getMetadata() {
        Optional<SpringMetadata> cached = cacheService.getCachedMetadata();

        if (cached.isPresent()) {
            return cached.get();
        }

        log.info("Fetching fresh metadata from Spring Initializr");
        SpringMetadata metadata = initializrClient.fetchMetadata();
        cacheService.cacheMetadata(metadata);

        return metadata;
    }

    public void refreshMetadata() {
        cacheService.clearCache();
        getMetadata();
    }
}
