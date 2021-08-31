package com.example.easydashcam;

/*
1. 압축은 정상적으로 동작
2. 압축을 통해 동영상의 용량을 줄이는 이슈 해결 x
3. ffmpeg 라이브러리를 우분투 서버 위에서 동작 시켜 해당 문제를 해결

 */


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class zipUtils {

    private static final int COMPRESSION_LEVEL=8;

    private static final int BUFFER_SIZE=1024*2;

    //=======================================================================================================
    public static void zip(String sourcePath, String output) throws Exception{

        File sourceFile=new File(sourcePath);

        if(!sourceFile.isFile()&&!sourceFile.isDirectory()){
            throw new Exception("cant find file for zip");
        }

        FileOutputStream fos=null;
        BufferedOutputStream bos=null;
        ZipOutputStream zos =null;  //파일을 압축 형태로 출력하기 위한 클래스 선언



        try {
            fos=new FileOutputStream(output);
            bos=new BufferedOutputStream(fos);
            zos=new ZipOutputStream(bos);

            zos.setLevel(COMPRESSION_LEVEL);    //사용자 지정 상수값 level 8
            zipEntry(sourceFile, sourcePath, zos);  // 사용자 지정 method -> sourcefile 과 zipStream 을 param으로 함

            zos.finish();

        }finally{
            // zos.finish()를 수행했음에도 스트림이 열려 있다면 닫는다. -> 역순으로 닫아서 에러가 발생하지 않도록 한다.
            if(zos!=null){
                zos.close();
            }
            if(bos!=null){
                zos.close();
            }
            if(fos!=null){
                fos.close();
            }
        }
    }// End of zip method
    //=======================================================================================================

    //sourceFile 이 디렉토리 인경우 하위 파일 리스트 가져와 재귀 호출 수행

    private static void zipEntry(File sourceFile, String sourcePath, ZipOutputStream zos) throws Exception{
        if(sourceFile.isDirectory()) {      //sourcefile이 directory 인 경우
            if (sourceFile.getName().equalsIgnoreCase(".metadata")) {
                return;
            }


            File[] fileArray = sourceFile.listFiles();

            for (int i = 0; i < fileArray.length; i++) {

                zipEntry(fileArray[i], sourcePath, zos);    //재귀 호출ㅇ

            }
        }else{  // sourcefile 이 directory가 아닌경우
            BufferedInputStream bis =null;

            try{
                String sFilePath=sourceFile.getPath();
                StringTokenizer tok=new StringTokenizer(sFilePath, "/"); // StringTokenizer 를 통해 /를 기준으로 파일명 파싱
                int tok_len=tok.countTokens();
                String zipEntryName=tok.toString();

                while(tok_len!=0){
                    tok_len--;
                    zipEntryName=tok.nextToken();
                }
                bis=new BufferedInputStream(new FileInputStream(sourceFile));



                ZipEntry zentry=new ZipEntry(zipEntryName);
                zentry.setTime(sourceFile.lastModified());
                zos.putNextEntry(zentry);



                byte[] buffer=new byte[BUFFER_SIZE];
                int cnt=0;



                while((cnt=bis.read(buffer, 0, BUFFER_SIZE))!=-1){
                    zos.write(buffer, 0, cnt);

                }
                zos.closeEntry();;


            }finally{
                if(bis!=null){
                    bis.close();

                }
            }
        }
    }
}
