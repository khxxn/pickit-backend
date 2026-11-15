package com.pickit.backend.popup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

// 최종 ERD에 없는 테이블이다. 교환글 찜(post_likes)과 같은 구조로 추가한다.
@Entity
@Table(name = "popup_likes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_popup_likes_user_popup", columnNames = {"user_id", "popup_id"})
})
public class PopupLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;

    // 회원 엔티티는 로그인 담당자가 관리한다. users FK는 수동 DDL로 연결한다.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "popup_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_popup_likes_popup"))
    private Popup popup;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected PopupLike() {
    }

    public PopupLike(Long userId, Popup popup, LocalDateTime createdAt) {
        this.userId = userId;
        this.popup = popup;
        this.createdAt = createdAt;
    }

    public Long getLikeId() {
        return likeId;
    }

    public Long getUserId() {
        return userId;
    }

    public Popup getPopup() {
        return popup;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
