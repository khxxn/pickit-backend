package com.pickit.backend.popup.repository;

import com.pickit.backend.popup.entity.Popup;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopupRepository extends JpaRepository<Popup, Long>, JpaSpecificationExecutor<Popup> {

    @Query(value = """
            SELECT nearby.*
            FROM (
                SELECT p.popup_id AS popupId,
                       p.name AS name,
                       p.thumbnail_url AS thumbnailUrl,
                       p.district AS district,
                       p.address AS address,
                       p.latitude AS latitude,
                       p.longitude AS longitude,
                       p.start_date AS startDate,
                       p.end_date AS endDate,
                       ST_Distance_Sphere(
                           POINT(CASE WHEN p.longitude = -180 THEN 180 ELSE p.longitude END, p.latitude),
                           POINT(:longitude, :latitude)
                       ) AS distanceMeters
                FROM popups p
            ) nearby
            WHERE nearby.distanceMeters <= :radiusMeters
            ORDER BY nearby.distanceMeters ASC, nearby.popupId ASC
            """, nativeQuery = true)
    List<NearbyPopupProjection> findNearbyPopups(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusMeters") int radiusMeters
    );
}
