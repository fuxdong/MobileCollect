#!/bin/bash
#/etc/init.d/mobileservice
### BEGIN INIT INFO
# Provides: mobileservice
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Required-Start: $remote_fs $syslog
# Required-Stop: $remote_fs $syslog
# Description: auto_run
### END INIT INFO
HOME='/home/pi/app/javaapp_home'
start(){
        cd $HOME && sh start_mobile.sh start
}
stop(){
    cd $HOME && sh start_mobile.sh stop
 }
 status(){
    cd $HOME && sh start_mobile.sh status
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
        echo "Usage：{start|stop|status}"
    ;;
esac
exit 0
