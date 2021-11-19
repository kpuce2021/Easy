#!/bin/bash
#
# mainScript - nodeScript / highlightScript 실행
#
#
##################################################
#
#	1. node check 스크립트 실행
#
##################################################

check=`ps | grep PGM_Extract | wc | awk '{print$1}'` # PGM_Extract 프로그램 실행시 1 출력

if (($check!=1))
##PGM_Extract
 then
  echo "     3. PGM_Extract is not running.."
  #   하이라이트 추출 프로그램 실행 
          else
  echo "     3. PGM_Extract is already running.."
	# 하이라이트 추출 프로그램 실행중  
fi


while ((1))
	do

echo -e ""
echo -e "		+----------------+"
echo -e "		|  script Start	 |"
echo -e "		+----------------+"
echo -e ""
echo -e "******************************************************\n"
	
	./nodeScript  
	# node check script 실행
	
echo -e "******************************************************\n"


#################################################
#
#	1. highlight 스크립트 실행
#	 
#################################################

	./highlightScript 
	# highlight extract Script 실행

#echo -e "\n"
echo -e "******************************************************\n"
#echo sleep 
sleep 10
#echo sleep 
done

#echo done
