package com.shop.service;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * 파일 처리 서비스
 *
 * @author 공통
 * @version 1.0
 */
@Log
@Service
public class FileService {

    /**
     * 파일 업로드 메소드
     *
     * @param uploadPath 파일 업로드 경로
     * @param originalFileName 원본파일 이름
     * @param fileData 파일 정보
     *
     * @return savedFileName 파일 업로드 후 업로드된 파일의 이름을 반환
     */
    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception {
        UUID uuid = UUID.randomUUID();

        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = uuid.toString() + extension;
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;

        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);

        fos.write(fileData);
        fos.close();

        return savedFileName;
    }

    /**
     * 파일 삭제 메소드
     *
     * @param filePath 파일의 경로
     *
     * @return 파일 삭제 처리 후 log 전송
     */
    public void deleteFile(String filePath) {
        File deleteFile = new File(filePath);

        if(deleteFile.exists()) {
            deleteFile.delete();

            log.info("파일을 삭제하였습니다.");
        } else {
            log.info("파일이 존재하지 않습니다.");
        }
    }

}
