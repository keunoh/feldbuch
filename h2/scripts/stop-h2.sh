#!/bin/bash

PID=$(ps -ef | grep "org.h2.tools.Server" | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
  echo "H2 Server is not running."
else
  echo "Stopping H2 Server (PID=$PID)"
  kill "$PID"
fi