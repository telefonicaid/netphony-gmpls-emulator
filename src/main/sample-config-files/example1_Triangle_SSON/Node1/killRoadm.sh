#!/bin/bash
ERPMPROC=`ps -ef | grep nodeDemo |grep -v grep | awk '{ print $2}' | head -1`
if [ -z "$ERPMPROC" ]; 
then
  echo "No process! ROADM is not running!"
else
  for pids in `ps -ef | grep nodeDemo |grep -v grep | awk '{ print $2}'`
  do 
    kill -9 $pids
  done
fi
