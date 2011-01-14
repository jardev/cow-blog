#!/bin/sh
echo "Deploying"

proj=$1

BASE=/opt/blog
INST=/opt/blog/active

echo "Unpacking to $BASE"
tar xjf $proj.tar.bz2 -C $BASE || (echo Failed to untar; exit $1)

echo "Loading dependencies"
cd $BASE/$proj
make deps

echo "Stopping services"
cd $INST
make stop

echo "Relinking $INST and copying old directories"
cp $INST/config_local.clj ~/
[ -h $INST ] && rm $INST && ln -s $BASE/$proj $INST
mv -f ~/config_local.clj $INST/

echo "Starting services"
cd $INST
make start

echo "Deployed"
exit 0
