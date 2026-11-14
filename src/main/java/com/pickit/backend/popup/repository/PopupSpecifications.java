package com.pickit.backend.popup.repository;

import com.pickit.backend.popup.dto.request.PopupListFilter;
import com.pickit.backend.popup.entity.Popup;
import com.pickit.backend.popup.entity.PopupRegion;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

public final class PopupSpecifications {

    private PopupSpecifications() {
    }

    public static Specification<Popup> filteredAndOrdered(
            PopupListFilter filter, LocalDate today, Set<Long> likedPopupIds
    ) {
        return (root, query, builder) -> {
            Path<LocalDate> startDate = root.get("startDate");
            Path<LocalDate> endDate = root.get("endDate");
            Predicate ongoing = builder.and(
                    builder.lessThanOrEqualTo(startDate, today),
                    builder.greaterThanOrEqualTo(endDate, today)
            );
            Predicate upcoming = builder.greaterThan(startDate, today);
            Predicate ended = builder.and(
                    builder.lessThanOrEqualTo(startDate, today),
                    builder.lessThan(endDate, today)
            );
            List<Predicate> conditions = new ArrayList<>();

            if (!filter.q().isEmpty()) {
                String keyword = filter.q().toLowerCase(Locale.ROOT)
                        .replace("!", "!!")
                        .replace("%", "!%")
                        .replace("_", "!_");
                conditions.add(builder.like(builder.lower(root.get("name")), "%" + keyword + "%", '!'));
            }

            if (!filter.statuses().isEmpty()) {
                List<Predicate> statuses = filter.statuses().stream()
                        .map(status -> switch (status) {
                            case ONGOING -> ongoing;
                            case UPCOMING -> upcoming;
                            case ENDED -> ended;
                        })
                        .toList();
                conditions.add(builder.or(statuses.toArray(Predicate[]::new)));
            }

            if (!filter.regions().isEmpty() && !filter.regions().contains(PopupRegion.NATIONWIDE)) {
                List<Predicate> regions = new ArrayList<>();
                for (PopupRegion region : filter.regions()) {
                    if (region.getAddressPrefixes().isEmpty()) {
                        regions.add(root.get("district").in(
                                region.getLabel(), "서울 " + region.getLabel()
                        ));
                    } else {
                        for (String prefix : region.getAddressPrefixes()) {
                            regions.add(builder.like(root.get("address"), prefix + "%"));
                        }
                    }
                }
                conditions.add(builder.or(regions.toArray(Predicate[]::new)));
            }

            if (!filter.categoryIds().isEmpty()) {
                conditions.add(root.get("category").get("categoryId").in(filter.categoryIds()));
            }

            if (query != null && query.getResultType() != Long.class) {
                List<Order> orders = new ArrayList<>();
                if (!likedPopupIds.isEmpty()) {
                    Expression<Integer> likeRank = builder.<Integer>selectCase()
                            .when(root.get("popupId").in(likedPopupIds), 0)
                            .otherwise(1);
                    orders.add(builder.asc(likeRank));
                }
                Expression<Integer> statusRank = builder.<Integer>selectCase()
                        .when(ongoing, 0)
                        .when(upcoming, 1)
                        .otherwise(2);
                orders.add(builder.asc(statusRank));
                orders.add(builder.desc(startDate));
                orders.add(builder.desc(root.get("popupId")));
                query.orderBy(orders);
            }

            return builder.and(conditions.toArray(Predicate[]::new));
        };
    }
}
