package com.nader.aiagent.repository;

import com.nader.aiagent.model.Platform;
import com.nader.aiagent.model.scrapedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapedPostRepository extends JpaRepository<scrapedPost,Long> {
    boolean existByPlatformAndExternalId(Platform platform, String externalId);

    Object countByPlatform(Platform platform);
}
