#!/bin/bash
#
# highlightScript - file comparison / file move / highlight extract / file delete
#
#############################################

#반복실행#
#영상 파일 하나씩 추출 과정 반복#


echo -e "       ------ highlightScript Srart ------\n"

echo -e "       1. 각 dir 이름 추출\n"
        videodirname=$(find ./ -name video_andToser)
echo -e "		video dirname 추출 =" $videodirname

        txtdirname=$(find ./ -name txt_andToser)
echo -e "		txt dir name 추출=" $txtdirname "\n"

  #######################################################################################
echo -e "       2. 각dir 첫 파일 추출\n"

        videofullname=$(ls $videodirname -t | tail -1)
#echo $videofullname
        videofirstname=$(basename "./video_andToser/"$videofullname .avi)
echo -e "		video dir first file name = " $videofirstname

        txtfullname=$(ls $txtdirname -t | tail -1)
#echo $txtfullname
        txtfirstname=$(basename "./txt_andToser/"$txtfullname .txt)
echo -e "		txt dir first file name =" $txtfirstname "\n"



  #######################################################################################
echo -e "       3. txtdir videodir 파일명 체크\n"

        if [ "$videofirstname" == "$txtfirstname" ]; then
echo -e "		videofirstname == txtfirstname 이 같음 \n"
                        # mv 로 둘다 현재 작업 폴더로 이동
                       mv $videodirname/$videofullname ./
                       mv $txtdirname/$txtfullname ./

echo -e "       4. 하이라이트 추출\n"
			./PGM_Extract $videofirstname			
		#!!!!!!!! ./VideoWrite0905 $인자로 videofirstname [수정]
echo -e "\n"

ExtractedfirstVideo=$(ls ./ExtractedVideo -tr | tail -1)

echo -e "	@@ DB insert\n"
		password="123"
echo "		insert into androidDB.videoTBL values('$ExtractedfirstVideo', '$videodirname');" | mysql -u"root" -p$password

	

echo -e "       5 . original file delete\n"
                       mv $videofullname ./AfterExtract
			mv $txtfullname ./AfterExtract


        elif [ "$videofirstname" != "$txtfirstname" ]; then
echo -e "		videofirstname == txtfirstname 이 같지 않음\n"
# break
        fi


echo -e "       ------ highlightScript End ------\n"















