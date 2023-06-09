package com.paymong.auth.global.client;

import com.paymong.auth.global.vo.request.FindMongReqVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "management")
public interface ManagementServiceClient {

    @GetMapping("/management/mong")
    ResponseEntity<Object> findMongByMember(@SpringQueryMap FindMongReqVo findMongReqVo);
}
