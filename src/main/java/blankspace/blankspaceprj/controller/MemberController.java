package blankspace.blankspaceprj.controller;

import blankspace.blankspaceprj.MemberVo;
import blankspace.blankspaceprj.dto.ResultDTO;
import blankspace.blankspaceprj.service.MemberServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/member/")
public class MemberController {

    @Autowired
    MemberServiceImpl userService;

    @RequestMapping(value = "findAll", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value="회원 목록 전체 조회", notes="회원 목록을 전체 조회하는 API")
    public ResponseEntity<?> findAllMember() throws Exception {
        ResultDTO responseDTO = new ResultDTO();
        responseDTO.setResultCode("S0001");
        responseDTO.setData(userService.findAll());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @ApiOperation(value="일반 회원 가입 수행", notes="일반 회원 가입을 수행하는 API")
    public ResponseEntity<?> joinMember(@RequestBody MemberVo memberVo) throws Exception {
        ResultDTO responseDTO = new ResultDTO();
        responseDTO.setResultCode("S0001");
        //responseDTO.setData(userService.findAll(memberVo));
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}