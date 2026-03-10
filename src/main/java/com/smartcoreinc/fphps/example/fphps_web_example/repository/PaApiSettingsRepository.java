package com.smartcoreinc.fphps.example.fphps_web_example.repository;

import com.smartcoreinc.fphps.example.fphps_web_example.entity.PaApiSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PA API 설정 영구 저장을 위한 JPA Repository
 */
@Repository
public interface PaApiSettingsRepository extends JpaRepository<PaApiSettings, Long> {

    /**
     * 가장 최근 PA API 설정을 조회
     */
    Optional<PaApiSettings> findTopByOrderByIdDesc();
}
