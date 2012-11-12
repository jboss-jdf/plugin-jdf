#!/bin/sh

# Require BASH 3 or newer

REQUIRED_BASH_VERSION=3.0.0

if [[ $BASH_VERSION < $REQUIRED_BASH_VERSION ]]; then
  echo "You must use Bash version 3 or newer to run this script"
  exit
fi

# Canonicalise the source dir, allow this script to be called anywhere
DIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

# DEFINE

# EAP team email subject
EAP_SUBJECT="\${RELEASEVERSION} of JDF Forge plugin released"
# EAP team email To ?
EAP_EMAIL_TO="jdf-dev@lists.jboss.org forge-dev@lists.jboss.org"
EMAIL_FROM="\"JDF Publish Script\" <benevides@redhat.com>"


# SCRIPT

usage()
{
cat << EOF
usage: $0 options

This script performs a release of the BOMs 

OPTIONS:
   -s      Snapshot version number to update from
   -n      New snapshot version number to update to, if undefined, defaults to the version number updated from
   -r      Release version number
EOF
}

notifyEmail()
{
   echo "***** Performing JDF Forge plugin release notifications"
   echo "*** Notifying JBoss JDF and Forge Dev lists team"
   subject=`eval echo $EAP_SUBJECT`
   echo "Email from: " $EMAIL_FROM
   echo "Email to: " $EAP_EMAIL_TO
   echo "Subject: " $subject
   # send email using /bin/mail
   echo "See \$subject :-)" | /usr/bin/env mail -r "$EMAIL_FROM" -s "$subject" "$EAP_EMAIL_TO"

}

release()
{
   echo "Releasing JDF Forge plugin version $RELEASEVERSION"
   $DIR/release-utils.sh -u -o $SNAPSHOTVERSION -n $RELEASEVERSION
   git commit -a -m "Prepare for $RELEASEVERSION release"
   git tag -a $RELEASEVERSION -m "Tag $RELEASEVERSION"
   $DIR/release-utils.sh -r
   $DIR/release-utils.sh -u -o $RELEASEVERSION -n $NEWSNAPSHOTVERSION
   git commit -a -m "Prepare for development of $NEWSNAPSHOTVERSION"
   git push upstrem HEAD --tags
   echo "***** JDF Forge plugin released"
   read -p "Do you want to send release notifcations to $EAP_EMAIL_TO[y/N]? " yn
   case $yn in
       [Yy]* ) notifyEmail;;
       * ) exit;
   esac
}

SNAPSHOTVERSION="UNDEFINED"
RELEASEVERSION="UNDEFINED"
NEWSNAPSHOTVERSION="UNDEFINED"

while getopts “n:r:s:” OPTION

do
     case $OPTION in
         h)
             usage
             exit
             ;;
         s)
             SNAPSHOTVERSION=$OPTARG
             ;;
         r)
             RELEASEVERSION=$OPTARG
             ;;
         n)
             NEWSNAPSHOTVERSION=$OPTARG
             ;;
         [?])
             usage
             exit
             ;;
     esac
done

if [ "$NEWSNAPSHOTVERSION" == "UNDEFINED" ]
then
   NEWSNAPSHOTVERSION=$SNAPSHOTVERSION
fi

if [ "$SNAPSHOTVERSION" == "UNDEFINED" -o  "$RELEASEVERSION" == "UNDEFINED" ]
then
   echo "\nMust specify -r and -s\n"
   usage
else  
   release
fi


