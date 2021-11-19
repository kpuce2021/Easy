#!/bin/bash
#
# mysql insert Script
#
###################################################

#echo mysql -e select * from users;

password="123"

echo "insert into androidDB.videoTBL values('test1', 'test1');" | mysql -u"root" -p$password



