#!/bin/bash
# chkconfig: 345 20 80
# description: Wisdom start/shutdown script
# processname: wisdom
#
# Instalation:
# copy file to /etc/init.d
# chmod +x /etc/init.d/wisdom
# chkconfig --add /etc/init.d/wisdom
# chkconfig wisdom on
#
# Usage: (as root)
# service wisdom start
# service wisdom stop
# service wisdom status
#
# You may want to temporarily remove the >/dev/null for debugging purposes
 
# Path to Wisdom application folder (to change)
WISDOM_HOME=/home/wisdom/wisdom
# Application mode.
APPLICATION_MODE="PROD"
# Path to the JVM (to change)
JAVA_HOME=/usr/java/latest/
# User running the Wisdom process (to change)
USER=wisdom



WISDOM=${WISDOM_HOME}/chameleon.sh
export JAVA_HOME


# source function library
. /etc/init.d/functions
RETVAL=0
 
start() {
	echo -n "Starting Wisdom service: "
	cd ${WISDOM_HOME}
	# Add the other system variable in the following line
	export JVM_ARGS="-Dapplication.mode=${APPLICATION_MODE}"
	su -s /bin/sh $USER -c "${WISDOM} start > /dev/null"
	RETVAL=$?
	
	if [ $RETVAL -eq 0 ]; then
		echo_success
	else
		echo_failure
	fi
	echo
}
stop() {
	echo -n "Shutting down Wisdom service: "
	cd ${WISDOM_HOME}
	${WISDOM} stop > /dev/null
	
	RETVAL=$?
 
	if [ $RETVAL -eq 0 ]; then
		echo_success
	else
		echo_failure
	fi
	echo
}
status() {
	cd ${WISDOM_HOME}
	${WISDOM} status
	RETVAL=$?
	
	if [ $RETVAL -eq 0 ]; then
		echo_success
	else
		echo_failure
	fi
	echo
}
clean() {
	cd ${WISDOM_HOME}
	${WISDOM} clean-all
    rm ${WISDOM_HOME}/RUNNING_PID
}
case "$1" in
	start)
	clean
	start
	;;
	stop)
	stop
	;;
	restart|reload)
	stop
	sleep 10
	start
	;;
	status)
	status
	;;
	clean)
	clean
	;;
	*)
	echo "Usage: $0 {start|stop|restart|status}"
esac
exit 0