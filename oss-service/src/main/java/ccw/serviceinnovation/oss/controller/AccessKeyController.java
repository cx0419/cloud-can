package ccw.serviceinnovation.oss.controller;

import ccw.serviceinnovation.common.request.ApiResp;
import ccw.serviceinnovation.oss.pojo.dto.AccessKeyDto;
import ccw.serviceinnovation.oss.pojo.dto.MessageDto;
import ccw.serviceinnovation.oss.service.IAccessKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Joy Yang
 *
 * 对象AccessKey 接口
 */
@RestController
@RequestMapping("/accessKey")
public class AccessKeyController {

    @Autowired
    IAccessKeyService accessKeyService;

    /**
     * 生成AccessKey
     * @return
     */
    @PostMapping("/createAccessKey")
    public ApiResp<Map<String, MessageDto>> createAccessKey(@RequestParam("objectId")Long objectId, @RequestParam("survivalTime")Long survivalTime){
        Map<String, MessageDto> accessKeys = accessKeyService.createAccessKey(objectId, survivalTime);
        return ApiResp.success(accessKeys);
    }

    /**
     * 获取该对象的 全部AccessKey
     * @param objectId
     * @return
     */
    @GetMapping("/getAccessKeys")
    public ApiResp<Map<String, MessageDto>> getAccessKeys(@RequestParam("objectId")Long objectId){
        Map<String, MessageDto> accessKeys = accessKeyService.getAccessKeys(objectId);
        return ApiResp.success(accessKeys);
    }

    /**
     * 删除对象 AccessKeyDto
     * @return
     */
    @DeleteMapping("/deleteAccessKey")
    public ApiResp<Map<String, MessageDto>> deleteAccessKey(@RequestBody AccessKeyDto accessKeyDto){
        Map<String, MessageDto> accessKeyMap = accessKeyService.deleteAccessKey(accessKeyDto);
        return ApiResp.success(accessKeyMap);
    }
}