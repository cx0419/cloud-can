package ccw.serviceinnovation.oss.controller;

import ccw.serviceinnovation.common.entity.Bucket;
import ccw.serviceinnovation.common.request.ApiResp;
import ccw.serviceinnovation.common.request.ResultCode;
import ccw.serviceinnovation.oss.pojo.vo.BucketVo;
import ccw.serviceinnovation.oss.pojo.vo.RPage;
import ccw.serviceinnovation.oss.service.IManageBucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理Bucket接口
 * @author 杨世博
 */
@RestController
@RequestMapping("/manageBucket")
public class ManageBucketController {

    @Autowired
    private IManageBucketService manageBucketService;

    /**
     * 获取bucket列表
     * 1-分页（无筛选）
     * 2-根据用户名、用户ID筛选
     * @param keyword
     * @param pageNum
     * @param size
     * @return
     */
    @GetMapping("/listBuckets")
    public ApiResp<RPage<BucketVo>> listBuckets(@RequestParam("keyword") String keyword,
                                                @RequestParam("pageNum")Integer pageNum,
                                                @RequestParam("size")Integer size){
        RPage<BucketVo> bucketRPage = manageBucketService.getBucketList(keyword,pageNum,size);
        return ApiResp.success(bucketRPage);
    }

    /**
     * 删除Bucket及其与之相关的所有信息所有
     * 1-收藏
     * @param userId Bucket拥有者的Id
     * @param name Bucket的名字
     * @return
     */
    @DeleteMapping("/deleteBucket")
    public ApiResp<Boolean> deleteBucket(@RequestParam("userId")Long userId,@RequestParam("name")String name) throws Exception{
        Boolean flag = manageBucketService.deleteBucket(userId, name);
        return ApiResp.ifResponse(flag,flag, ResultCode.COMMON_FAIL);
    }
}
