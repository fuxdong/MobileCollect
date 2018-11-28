#!/bin/bash
#description: 启动重启server服务 
#端口号，根据此端口号确定PID  
PORT=2000  
HOST=127.0.0.1
SERIAL=1
#启动命令所在目录  
HOME='/home/pi/app/javaapp_home'  

#查询出监听了PORT端口TCP协议的程序  
pid=`netstat -anp|grep $PORT|awk '{printf $7}'|cut -d/ -f1`  
   
   
start(){  
	if [ -n "$pid" ]; then  
	   echo "server already start,pid:$pid"  
	   return 0
	fi
	#进入命令所在目录  
	cd $HOME  
	nohup java -jar $HOME/mobileCollect host=$HOST port=$PORT serial=$SERIAL > $HOME/server.log 2>&1 &   #启动聊天服务器 把日志输出到HOME目录的server.log文件中   
	echo "start at port:$PORT"  
} 

stop(){  
    if [ -z "$pid" ]; then  
       echo "not find program on port:$PORT"  
       return 0
    fi
    #结束程序，使用讯号2，如果不行可以尝试讯号9强制结束  
    kill -9 $pid
    rm -rf $pid
    echo "kill program use signal 2,pid:$pid"  
 } 
 
 status(){  
    if [ -z "$pid" ]; then  
       echo "not find program on port:$PORT"  
    else  
       echo "program is running,pid:$pid"  
    fi  
 }  
   
 case $1 in  
    start)  
       start  
    ;;  
    stop)  
       stop  
    ;;
    restart)  
       $0 stop
       sleep 2
       $0 start
     ;;
    status)  
       status  
    ;;  
    *)  
       echo "Usage: {start|stop|status}"  
    ;;  
 esac  
   
 exit 0  
