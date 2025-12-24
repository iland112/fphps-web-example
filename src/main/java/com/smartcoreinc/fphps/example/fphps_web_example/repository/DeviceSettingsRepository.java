package com.smartcoreinc.fphps.example.fphps_web_example.repository;

import com.smartcoreinc.fphps.example.fphps_web_example.entity.DeviceSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 디바이스 설정 영구 저장을 위한 JPA Repository
 */
@Repository
public interface DeviceSettingsRepository extends JpaRepository<DeviceSettings, Long> {

    /**
     * 가장 최근 설정을 조회 (ID가 가장 큰 레코드)
     * 애플리케이션에서는 단일 설정만 사용
     */
    Optional<DeviceSettings> findTopByOrderByIdDesc();
}
