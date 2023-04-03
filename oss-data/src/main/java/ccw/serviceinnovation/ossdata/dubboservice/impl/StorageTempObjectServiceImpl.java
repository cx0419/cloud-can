package ccw.serviceinnovation.ossdata.dubboservice.impl;

import ccw.serviceinnovation.common.constant.FileTypeConstant;
import ccw.serviceinnovation.common.constant.SecretEnum;
import ccw.serviceinnovation.common.util.file.JpegCompress;
import ccw.serviceinnovation.common.util.file.VideoCompress;
import ccw.serviceinnovation.common.util.sm4.SM4;
import ccw.serviceinnovation.common.util.sm4.SM4Utils;
import ccw.serviceinnovation.ossdata.constant.OssDataConstant;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import service.StorageTempObjectService;
import service.bo.FilePrehandleBo;

import java.io.*;
import java.util.Arrays;

import static ccw.serviceinnovation.common.constant.FileConstant.READ_WRITER_SIZE;
import static ccw.serviceinnovation.ossdata.constant.FilePrefixConstant.FILE_NOR;
import static ccw.serviceinnovation.ossdata.constant.FilePrefixConstant.FILE_TMP_BLOCK;
import static ccw.serviceinnovation.ossdata.constant.OssDataConstant.POSITION;

/**
 * 缓存实现类
 * @author 陈翔
 */
@DubboService(version = "1.0.0", group = "temp",interfaceClass = StorageTempObjectService.class)
@Slf4j
@Service
public class StorageTempObjectServiceImpl implements StorageTempObjectService {
    private String TMP_BLOCK =  POSITION + "\\" + FILE_TMP_BLOCK;
    private String NOR = POSITION + "\\" + FILE_NOR;

    @Override
    public FilePrehandleBo preHandle(String etag,String objectKey,Boolean press,Integer secret) {
        File file = new File(TMP_BLOCK + objectKey);
        //获取文件类型
        String type = FileTypeUtil.getType(file);
        FilePrehandleBo fileCompressBo = new FilePrehandleBo();
        //当需要进行etag判断时
        if(etag!=null){
            log.info("需要进行etag判断");
            //设置老的etag
            log.info("正在计算md5:{}",file.getAbsolutePath());
            String backendEtag = SecureUtil.md5(file);
            fileCompressBo.setOldEtag(backendEtag);
            log.info("前端etag:{},后端etag:{}",etag,backendEtag);
            if(secret!=null){
                //有加密时无需验证
                return fileCompressBo;
            }
            if(!backendEtag.equals(etag)){
                log.info("前后端hash验证失败");
                return null;
            }
        }


        if("mp4".equals(type)){
            fileCompressBo.setFileType(FileTypeConstant.VIDEO);
            if(press){
                String newName = UUID.randomUUID().toString().replace("-", "_");
                log.info("newName:{}",newName);
                //生成mp4压缩文件
                File file1 = VideoCompress.compressionVideo(file, newName);
                System.out.println(POSITION + "\\"+newName);
                //获取这个文件的md5
                String newETag = SecureUtil.md5(file1);
                log.info("原来token:{},的etag:{},压缩完成:{},etag为:{}",objectKey,etag,file1.getAbsolutePath(),newETag);
                fileCompressBo.setNewEtag(newETag);
                file1.renameTo(new File(TMP_BLOCK+objectKey));
            }
        }else if("txt".equals(type)){
            fileCompressBo.setFileType(FileTypeConstant.TEXT);
            return fileCompressBo;
        }else if("jpg".equals(type)){
            fileCompressBo.setFileType(FileTypeConstant.IMG);
            if (press){
                JpegCompress.compress(TMP_BLOCK + objectKey, TMP_BLOCK + objectKey);
                String eTag = SecureUtil.md5(new File(TMP_BLOCK + objectKey));
                log.info("原来的token:{},的etag:{},压缩完成以后的路径:{},新etag为:{}",objectKey,etag,TMP_BLOCK + objectKey,eTag);
                fileCompressBo.setNewEtag(eTag);
            }
        }else{
            fileCompressBo.setFileType(FileTypeConstant.OTHER);
        }
        log.info("识别为:{}", fileCompressBo.getFileType());
        return fileCompressBo;
    }

    public static void main(String[] args) {
        File file = new File("D:\\OSS\\02\\position\\TMP_BLOCK&aaac15f9_1006_463a_933f_a45768c18f08");
        JpegCompress.compress("D:\\OSS\\02\\position\\TMP_BLOCK&aaac15f9_1006_463a_933f_a45768c18f08", "123");
    }

    @Override
    public String getPort() {
        return OssDataConstant.PORT;
    }


    private Boolean reName(File oldName,File newName) throws Exception{
        if (newName.exists()) {
            throw new java.io.IOException("file exists");
        }
        if(oldName.renameTo(newName)) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public Boolean blockBecomeFullMember(String token,String objectKey) throws Exception {
        return reName(new File(TMP_BLOCK + token),new File(NOR + objectKey));
    }

    private Boolean  deleteFile(File file){
        if(file.exists() && file.isDirectory() && file.list()!=null){
            String[] list = file.list();
            assert list != null;
            if(list.length==0){
                return file.delete();
            }else{
                return false;
            }
        }else{
            return false;
        }
    }



    @Override
    public Boolean deleteBlockObject(String objectKey) throws Exception {
        File file = new File(TMP_BLOCK +objectKey);
        return deleteFile(file);
    }


    @Override
    public Boolean saveBlock(String blockToken, Long targetSize, byte[] bytes, Long srcSize, Integer chunks, Integer chunk,Integer secret) throws IOException {
        RandomAccessFile randomAccessFile;
        randomAccessFile = new RandomAccessFile(TMP_BLOCK + blockToken,"rw");
        if(secret==null){
            randomAccessFile.setLength(targetSize);
            long offset = 0;
            if (chunk == chunks - 1 && chunk != 0) {
                offset = chunk * (targetSize - srcSize) / chunk;
            } else {
                offset = chunk * srcSize;

            }
            randomAccessFile.seek(offset);
            log.info("偏移量:{}",offset);
            randomAccessFile.write(bytes,0,bytes.length);
            randomAccessFile.close();
        }else if(secret.equals(SecretEnum.SM4.getCode())){
            SM4Utils sm4Utils = new SM4Utils();
            //加密以后的文件长度
            srcSize = sm4Utils.getAfterSecretLength(srcSize);
            try {
                randomAccessFile.setLength(targetSize);
                if (chunk == chunks - 1 && chunk != 0) {
                    randomAccessFile.seek(chunk * (targetSize - srcSize) / chunk);
                } else {
                    randomAccessFile.seek(chunk * srcSize);
                }
                //每次写入的大小改变
                int byteSize = (int) sm4Utils.getAfterSecretLength(READ_WRITER_SIZE);
                int len = byteSize;
                for (int i = 0; i < bytes.length; i+= byteSize) {
                    //正常来说 i+byteSize = bytes.length
                    if(i+byteSize > bytes.length){
                        len = bytes.length - i + 1;
                    }
                    randomAccessFile.write(bytes, i, len);
                }

            } finally {
                randomAccessFile.close();
            }
        }
        return true;
    }




}
