package com.pickit.backend.popup.dto.request;

import com.pickit.backend.popup.entity.PopupRegion;
import com.pickit.backend.popup.entity.PopupStatus;
import java.util.Set;

public record PopupListFilter(
        Set<PopupStatus> statuses,
        Set<PopupRegion> regions,
        Set<Long> categoryIds
) {
    public PopupListFilter {
        statuses = statuses == null ? Set.of() : Set.copyOf(statuses);
        regions = regions == null ? Set.of() : Set.copyOf(regions);
        categoryIds = categoryIds == null ? Set.of() : Set.copyOf(categoryIds);
    }
}
