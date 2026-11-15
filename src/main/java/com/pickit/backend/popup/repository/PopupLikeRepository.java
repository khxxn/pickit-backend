package com.pickit.backend.popup.repository;

import com.pickit.backend.popup.entity.PopupLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopupLikeRepository extends JpaRepository<PopupLike, Long> {

    boolean existsByUserIdAndPopup_PopupId(Long userId, Long popupId);

    long countByPopup_PopupId(Long popupId);

    @Query("select l.popup.popupId from PopupLike l where l.userId = :userId")
    List<Long> findPopupIdsByUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PopupLike l where l.userId = :userId and l.popup.popupId = :popupId")
    int deleteByUserIdAndPopupId(@Param("userId") Long userId, @Param("popupId") Long popupId);
}
