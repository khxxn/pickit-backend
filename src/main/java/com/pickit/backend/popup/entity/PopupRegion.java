package com.pickit.backend.popup.entity;

import java.util.List;

public enum PopupRegion {
    SEOUL_ALL("서울 전체", "서울"),
    GANGNAM_SEOCHO("강남/서초"),
    HONGDAE_SINCHON("홍대/신촌"),
    JONGNO_GWANGHWAMUN("종로/광화문"),
    SEONGSU("성수"),
    YONGSAN("용산"),
    YEOUIDO("여의도"),
    JAMSIL("잠실"),
    GYEONGGI_INCHEON("경기/인천", "경기", "인천"),
    CHUNGCHEONG("충청권", "충북", "충남", "충청", "대전", "세종"),
    GYEONGSANG("경상권", "경북", "경남", "경상", "부산", "대구", "울산"),
    JEOLLA("전라권", "전북", "전남", "전라", "광주"),
    GANGWON("강원권", "강원"),
    JEJU("제주권", "제주"),
    NATIONWIDE("전국");

    private final String label;
    private final List<String> addressPrefixes;

    PopupRegion(String label, String... addressPrefixes) {
        this.label = label;
        this.addressPrefixes = List.of(addressPrefixes);
    }

    public String getLabel() {
        return label;
    }

    public List<String> getAddressPrefixes() {
        return addressPrefixes;
    }
}
