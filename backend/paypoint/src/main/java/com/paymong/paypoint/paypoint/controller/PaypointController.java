package com.paymong.paypoint.paypoint.controller;

import com.paymong.paypoint.global.code.PaypointStateCode;
import com.paymong.paypoint.global.response.ErrorResponse;
import com.paymong.paypoint.paypoint.dto.AddPayReqDto;
import com.paymong.paypoint.paypoint.dto.AddPointReqDto;
import com.paymong.paypoint.paypoint.service.PaypointService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/paypoint")
public class PaypointController {
    final public PaypointService paypointService;
    @PostMapping("")
    public ResponseEntity<Object> addPay(@RequestHeader(value = "MemberId") String memberIdStr,
                                              @RequestHeader(value = "MongId") String mongIdStr,
                                              @RequestBody AddPayReqDto addPaypointReqDto
                                              ){
        log.info("addPay - Call");
        try {
            paypointService.addPay(memberIdStr, mongIdStr, addPaypointReqDto);
            return ResponseEntity.status(HttpStatus.OK).body("zz");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(PaypointStateCode.UNKNOWN));
        }
    }

    @PostMapping("/point")
    public ResponseEntity<Object> addPoint(@RequestBody AddPointReqDto addPointReqDto){
        log.info("addPoint - Call");
        try {
            paypointService.addPoint(addPointReqDto);
            return ResponseEntity.status(HttpStatus.OK).body("");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(PaypointStateCode.UNKNOWN));
        }
    }


    @GetMapping("/list")
    public ResponseEntity<Object> findAllPaypoint(@RequestHeader(value = "MemberId") String memberIdStr){
        log.info("findAllPaypoint - Call");
        try {
            paypointService.findAllPaypoint(memberIdStr);
        }catch (Exception e){

        }
        return null;
    }

}
