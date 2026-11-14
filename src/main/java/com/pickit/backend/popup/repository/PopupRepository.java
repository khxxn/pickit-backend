package com.pickit.backend.popup.repository;

import com.pickit.backend.popup.entity.Popup;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopupRepository extends JpaRepository<Popup, Long> {

    @Query(value = """
            select p from Popup p
            order by
                case
                    when p.startDate <= :today and p.endDate >= :today then 0
                    when p.startDate > :today then 1
                    else 2
                end,
                p.startDate desc,
                p.popupId desc
            """, countQuery = "select count(p) from Popup p")
    Page<Popup> findPopupList(@Param("today") LocalDate today, Pageable pageable);

    @Query(value = """
            select p from Popup p
            order by
                case when p.popupId in :likedPopupIds then 0 else 1 end,
                case
                    when p.startDate <= :today and p.endDate >= :today then 0
                    when p.startDate > :today then 1
                    else 2
                end,
                p.startDate desc,
                p.popupId desc
            """, countQuery = "select count(p) from Popup p")
    Page<Popup> findPopupListWithLikes(
            @Param("today") LocalDate today,
            @Param("likedPopupIds") Set<Long> likedPopupIds,
            Pageable pageable
    );
}
