#! /bin/bash

# initialize...
project=HermiT
date=`date +%Y%m%d`
time=`date +%H:%M:%S`

basedir=/home/scratch/$project
javadir=/usr  #$basedir/jdk1.6.0_18
antdir=$basedir/apache-ant-1.8.1
builddir=$basedir/workspace/$project
bindir=$basedir/nightlybuilds
reportdir=$basedir/reports/$date
logdir=$basedir/logs/$date
log=$logdir/time:$time.txt

mkdir -p $basedir
mkdir -p $builddir
mkdir -p $builddir/build
mkdir -p $bindir
mkdir -p $reportdir
mkdir -p $logdir

# update user profile to have required executables on the path (just to be on the safe side)
echo #! /bin/bash > ~/.profile
echo export JAVA_HOME=$javadir >> ~/.profile
echo export ANT_HOME=$antdir >> ~/.profile
echo export PATH=$PATH:$ANT_HOME/bin:$JAVA_HOME/bin >> ~/.profile

# import new settings
. ~/.profile 

echo Good morning Dr. Chandra. This is HAL. Here is the report you asked me to generate. 
echo -------------------------------------------- >> $log
echo Starting build of $project at $time >> $log

# STDIN: 0, STDOUT: 1, STDERR: 2, ...
# redirect uses &
# 2>&1 redirects STDERR output to STDOUT
cd $builddir 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
  
echo Working directory: $builddir >> $log
echo >> $log    #empty line
  
#echo Cleaning up old logs .. >> $log
#find -name '*.log' -or -name build | xargs rm -Rf 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
  
echo Updating files from SVN 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
echo -------------------------------------------- >> $log
eval svn co -q svn://edison.comlab.ox.ac.uk/krr/2008/$project/trunk $builddir 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log

echo Running ant 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
echo -------------------------------------------- >> $log
eval ant -q 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log

echo Moving and renaming build 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
echo -------------------------------------------- >> $log
eval mv $builddir/build/$project*.zip $bindir/$project-$date.zip 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log

echo Running tests 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
echo -------------------------------------------- >> $log
eval ant -q test-hard 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
eval cat $builddir/build/testreports/raw/AllTests.txt >> $log

echo Copying JUnit HTML report... 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
echo -------------------------------------------- >> $log
eval mv $builddir/build/testreports/html/junit-noframes.html $reportdir/$project-JUnitResults-$date.html 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log

echo Attempting to clean old logs out... >> $log
DATE=$(date -d last-week +"%Y%m%d")
cd $logdir/..
files=$(find . \
  -maxdepth 1 -type d \
  -name $DATE )
if [ -n "$files" ]; then
  echo "deleting..." $files >> $log
  rm -Rf $files 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
fi
echo -------------------------------------------- >> $log 
echo Attempting to delete old nightly builds... >> $log
cd $bindir
files=$(find . \
  -maxdepth 1 -type f \
  -name $project-$DATE.zip )
if [ -n "$files" ]; then
  echo "deleting..." $files >> $log
  rm -Rf $files 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
fi  
echo -------------------------------------------- >> $log
echo Attempting to delete old JUnit reports... >> $log
cd $reportdir/..
files=$(find . \
  -maxdepth 1 -type d \
  -name $DATE )
if [ -n "$files" ]; then
  echo "deleting..." $files >> $log
  rm -Rf $files 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
fi
echo -------------------------------------------- >> $log
echo Publishing the nightly build and report... >> $log
eval scp $bindir/$project-$date.zip birg@edison.comlab.ox.ac.uk:/data/hermit/download/nightlybuilds/ 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
eval scp $reportdir/$project-JUnitResults-$date.html birg@edison.comlab.ox.ac.uk:/data/hermit/download/nightlybuilds/ 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log
echo -------------------------------------------- >> $log
echo Removing old nightly builds and reports from edison >> $log
eval ssh birg@edison.comlab.ox.ac.uk '/data/hermit/scripts/rmOldNightlyBuilds.sh' 2>&1 3>&1 4>&1 5>&1 6>&1 7>&1 8>&1 9>&1 >> $log 
echo -------------------------------------------- >> $log
echo >> $log

cat $log
