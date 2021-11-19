#!/bin/bash
#
# nodeScript - nodecheck / noderun
#
#########################################
#
#	1. node run check
#

#echo -e "\n"
#echo -e "******************************************************\n"
echo -e "	------ nodeScript Start ------\n"
PGM_NAME=node

#node run check
echo "     1. $PGM_NAME run check"
#echo "          result [1] == already running "
#echo -e "          result [other] == not running\n"

	check=`ps -ef | grep $PGM_NAME | wc | awk '{print$1}'`
# 	check=`ps | grep $PGM_NAME | wc | awk '{print$1}'`
	
echo -e "     2. result [" $check "]\n"


#Start node
	if (($check!=1))
	then
#echo "     3. $PGM_NAME is not running.. let's run it now!"
   nohup node server.js &
	else
echo "     3. $PGM_NAME is already running.."
	fi

echo -e ""
echo -e "	------ nodeScript End ------\n"


 #source code has already been compiled
