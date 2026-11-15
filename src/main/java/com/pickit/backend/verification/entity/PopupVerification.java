package com.pickit.backend.verification.entity;

import com.pickit.backend.popup.entity.Popup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "popup_verifications", indexes = {
        @Index(name = "idx_popup_verifications_user_popup", columnList = "user_id,popup_id,verification_id")
})
public class PopupVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    // 회원 엔티티는 로그인 담당자가 관리한다. users FK는 수동 DDL 및 사용자 조회로 연결한다.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "popup_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_popup_verifications_popup"))
    private Popup popup;

    @Column(name = "verified_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal verifiedLat;

    @Column(name = "verified_lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal verifiedLng;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "last_lat", precision = 10, scale = 7)
    private BigDecimal lastLat;

    @Column(name = "last_lng", precision = 10, scale = 7)
    private BigDecimal lastLng;

    @Column(name = "last_location_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime lastLocationAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    protected PopupVerification() {
    }

    public PopupVerification(Long userId, Popup popup, BigDecimal lat, BigDecimal lng,
            LocalDateTime verifiedAt, LocalDateTime measuredAt, LocalDateTime expiredAt) {
        this.userId = userId;
        this.popup = popup;
        this.verifiedLat = lat;
        this.verifiedLng = lng;
        this.verifiedAt = verifiedAt;
        this.expiredAt = expiredAt;
        recordLocation(lat, lng, measuredAt);
    }

    public void recordLocation(BigDecimal lat, BigDecimal lng, LocalDateTime measuredAt) {
        this.lastLat = lat;
        this.lastLng = lng;
        this.lastLocationAt = measuredAt;
    }

    public boolean isVerifiedAt(LocalDateTime now) {
        return leftAt == null && expiredAt.isAfter(now);
    }

    public void extendUntil(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public void leaveAt(LocalDateTime now) {
        if (leftAt == null) {
            leftAt = now;
            if (expiredAt.isAfter(now)) {
                expiredAt = now;
            }
        }
    }

    public Long getVerificationId() { return verificationId; }
    public Long getUserId() { return userId; }
    public Popup getPopup() { return popup; }
    public BigDecimal getVerifiedLat() { return verifiedLat; }
    public BigDecimal getVerifiedLng() { return verifiedLng; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public BigDecimal getLastLat() { return lastLat; }
    public BigDecimal getLastLng() { return lastLng; }
    public LocalDateTime getLastLocationAt() { return lastLocationAt; }
    public LocalDateTime getLeftAt() { return leftAt; }
}
