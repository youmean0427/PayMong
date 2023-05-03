package com.paymong.collect.global.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorStateCode {
    MAP_RUNTIME("001", "맵 컬렉션을 찾을 수 없습니다."),
    MONG_RUNTIME("002", "몽 컬렉션을 찾을 수 없습니다."),
    BAD_GATEWAY("502", "게이트 웨이 에러"),
    RUNTIME("500", "서버 에러");
    private final String code;
    private final String message;


}
