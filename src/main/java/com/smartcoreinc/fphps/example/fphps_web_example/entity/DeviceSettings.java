package com.smartcoreinc.fphps.example.fphps_web_example.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 디바이스 설정을 영구 저장하기 위한 JPA 엔티티
 * SQLite 데이터베이스에 설정값을 저장하여 애플리케이션 재시작 후에도 유지
 */
@Entity
@Table(name = "device_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기본 설정
    @Column(name = "check_remove")
    private boolean checkRemove;

    @Column(name = "enable_rf")
    private boolean enableRF;

    @Column(name = "rf_read_size")
    private int rfReadSize;

    @Column(name = "enable_barcode")
    private boolean enableBarcode;

    @Column(name = "enable_id_card")
    private boolean enableIDCard;

    @Column(name = "rf_use_sfi")
    private boolean rfUseSFI;

    @Column(name = "security_check")
    private boolean securityCheck;

    // 이미지 향상 설정
    @Column(name = "enable_enhance_ir")
    private boolean enableEnhanceIR;

    @Column(name = "enable_enhance_uv")
    private boolean enableEnhanceUV;

    @Column(name = "enable_enhance_wh")
    private boolean enableEnhanceWH;

    // Anti-Glare 설정
    @Column(name = "anti_glare")
    private boolean antiGlare;

    @Column(name = "anti_glare_ir")
    private boolean antiGlareIR;

    @Column(name = "anti_glare_ir_half")
    private boolean antiGlareIRHalf;

    // 강도 설정
    @Column(name = "strength_wh")
    private int strengthWH;

    @Column(name = "strength_ir")
    private int strengthIR;

    @Column(name = "crop")
    private boolean crop;

    // Batch Mode (이미지 캡처 모드)
    @Column(name = "batch_ir")
    private boolean ir;

    @Column(name = "batch_uv")
    private boolean uv;

    @Column(name = "batch_wh")
    private boolean wh;

    // E-Passport Data Groups
    @Column(name = "dg1")
    private boolean dg1;
    @Column(name = "dg2")
    private boolean dg2;
    @Column(name = "dg3")
    private boolean dg3;
    @Column(name = "dg4")
    private boolean dg4;
    @Column(name = "dg5")
    private boolean dg5;
    @Column(name = "dg6")
    private boolean dg6;
    @Column(name = "dg7")
    private boolean dg7;
    @Column(name = "dg8")
    private boolean dg8;
    @Column(name = "dg9")
    private boolean dg9;
    @Column(name = "dg10")
    private boolean dg10;
    @Column(name = "dg11")
    private boolean dg11;
    @Column(name = "dg12")
    private boolean dg12;
    @Column(name = "dg13")
    private boolean dg13;
    @Column(name = "dg14")
    private boolean dg14;
    @Column(name = "dg15")
    private boolean dg15;
    @Column(name = "dg16")
    private boolean dg16;

    // E-Passport Authentication
    @Column(name = "auth_pa")
    private boolean pa;
    @Column(name = "auth_aa")
    private boolean aa;
    @Column(name = "auth_ca")
    private boolean ca;
    @Column(name = "auth_ta")
    private boolean ta;
    @Column(name = "auth_sac")
    private boolean sac;
}
