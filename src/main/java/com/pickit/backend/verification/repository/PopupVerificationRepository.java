package com.pickit.backend.verification.repository;

import com.pickit.backend.verification.entity.PopupVerification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopupVerificationRepository extends JpaRepository<PopupVerification, Long> {

    @Query("""
            select v from PopupVerification v
            join fetch v.popup
            where v.userId = :userId
              and v.verificationId in (
                  select max(latest.verificationId) from PopupVerification latest
                  where latest.userId = :userId
                  group by latest.popup.popupId
              )
            order by v.popup.popupId
            """)
    List<PopupVerification> findLatestByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = "popup")
    Optional<PopupVerification> findFirstByUserIdAndPopup_PopupIdOrderByVerificationIdDesc(
            Long userId, Long popupId);
}
