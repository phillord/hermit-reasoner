#! /bin/bash

cd /data/hermit/download/nightlybuilds/

DATE=$(date -d last-week +"%Y%m%d")

files=$(find . \
  -maxdepth 1 -type f \
  -name HermiT-$DATE.zip )
 
if [ -n "$files" ]; then
  rm -Rf $files 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> /dev/null
fi

files=$(find . \
  -maxdepth 1 -type f \
  -name HermiT-JUnitResults-$DATE.html )

if [ -n "$files" ]; then
  rm -Rf $files 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> /dev/null
fi
