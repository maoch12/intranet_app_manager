#!/bin/bash
source /etc/profile

project_name="intranent_app_manager"

project_path=$(cd `dirname $0`; pwd)
project_jar="${project_name}.jar"
project_log="${project_name}.log"

#这个命令也可以kill掉
#grep -v grep :grep -v意思是不包括，查找出了grep的所有信息
#awk '{print $2}' 取出第二个字段
#xargs将标准输入转化为命令行参数：echo '4567'|xargs kill -9 等同于：echo '4567' ;kill -9 4567
#xargs 是给命令传递参数的一个过滤器，也是组合多个命令的一个工具。一般是和管道一起使用。
#xargs 是一个强有力的命令，它能够捕获一个命令的输出，然后传递给另外一个命令。之所以能用到这个命令，关键是由于很多命令不支持|管道来传递参数，而日常工作中有有这个必要，所以就有了 xargs 命令
#ps -ef|grep $project_jar|grep -v grep|awk '{print $2}'|xargs kill -9

usage() {
 echo "Usage: sh $0 [start|stop|restart|status]"
 exit 1
}

#检查程序是否在运行
is_exist(){
 pid=`ps -ef|grep $project_jar|grep -v grep|awk '{print $2}' `
 #如果不存在返回1，存在返回0
 if [ -z "${pid}" ]; then
     return 1
 else
     return 0
 fi
}

#启动
start(){
 is_exist
 if [ $? -eq "0" ]; then
     echo "${project_name} is already running. Pid is ${pid}."
 else
     echo "${project_name} Start."
     nohup java -jar  $project_path/$project_jar > $project_path/$project_log 2>&1 &
 fi
}

#停止
stop(){
 is_exist

 if [ $? -eq "0" ]; then
     echo "${project_name} Stop."
     kill -15 $pid
 else
     echo "${project_name} is not running."
 fi
}

#输出运行状态
status(){
 is_exist
 if [ $? -eq "0" ]; then
     echo "${project_name} is running. Pid is ${pid}."
 else
     echo "${project_name} is not running."
 fi
}

#重启
restart(){
 stop
 sleep 3
 start
}

#根据输入参数，选择执行对应方法，不输入则执行使用说明
case "$1" in
 "start")
 start
 ;;
 "stop")
 stop
 ;;
 "status")
 status
 ;;
 "restart")
 restart
 ;;
 *)
 usage
 ;;
esac