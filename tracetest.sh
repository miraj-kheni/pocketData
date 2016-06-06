
adb reboot
sleep 15s
adb root
sleep 2m
i=1

for j in `seq 1 1`;
do
        adb shell rm /data/data/com.example.SqliteTrace/databases/*
        adb shell sh /data/sql.sh #create database
        adb shell sh /data/sql.sh #run the queries
        adb pull /data/trace.log
        mv trace.log $i$j.log
done

