#!/bin/bash -f
# about:  AWIPS install manager
# devorg: Unidata Program Center
# author: <mjames@ucar.edu>
# use: ./install.sh (--cave|--edex|--ingest|--help)

dir="$( cd "$(dirname "$0")" ; pwd -P )"

usage="$(basename "$0") [-h] (--cave|--edex|--ingest) #script to install Unidata AWIPS components.\n
    -h, --help           show this help text\n
    --cave               install CAVE for x86_64 Linux\n
    --edex, --server     install EDEX Standaone Server for x86_64 Linux\n
    --ingest             install EDEX Ingest Node Server for x86_64 Linux\n"

function stop_edex_services {
  for srvc in edex_ldm edex_camel qpidd httpd-pypies edex_postgres ; do
    if [ -f /etc/init.d/$srvc ]; then
      service $srvc stop
    fi
  done
}

function check_yumfile {
  if [ ! -f /etc/yum.repos.d/awips2.repo ]; then
    if [[ $(grep "release 7" /etc/redhat-release) ]]; then
      repofile=el7.repo
    else
      repofile=awips2.repo
    fi
    wget_url="https://www.unidata.ucar.edu/software/awips2/doc/${repofile}"
    echo "wget -O /etc/yum.repos.d/awips2.repo ${wget_url}"
    wget -O /etc/yum.repos.d/awips2.repo ${wget_url}
  fi
  yum clean all --enablerepo=awips2repo --disablerepo="*" 1>> /dev/null 2>&1
}

function check_limits {
  if [[ ! $(grep awips /etc/security/limits.conf) ]]; then
    echo "Checking /etc/security/limits.conf for awips: Not found. Adding..."
    printf "awips soft nproc 65536\nawips soft nofile 65536\n" >> /etc/security/limits.conf
  fi
}

function check_netcdf {
  if [[ $(rpm -qa | grep netcdf-AWIPS) ]]; then
    # replaced by epel netcdf(-devel) pkgs in 17.1.1-5 so force remove
    yum remove netcdf-AWIPS netcdf netcdf-devel -y
  fi
}

function check_edex {
  if [[ $(rpm -qa | grep awips2-edex) ]]; then
    echo "found EDEX RPMs installed. Updating..."
  else
    if [ -d /awips2/data/ ]; then
      echo "cleaning up /awips2/data/ for new install..."
      rm -rf /awips2/data/
    fi
  fi
  for dir in /awips2/tmp /awips2/data_store ; do
    if [ ! -d $dir ]; then
      echo "creating $dir"
      mkdir -p $dir
      chown awips:fxalpha $dir
    fi
  done
  if getent passwd awips &>/dev/null; then
    echo -n ''
  else
    echo
    echo "--- user awips does not exist"
    echo "--- installation will continue but EDEX services may not run as intended"
  fi
}

function server_prep {
  check_yumfile
  stop_edex_services
  check_limits
  check_netcdf
  check_edex
}


if [ $# -eq 0 ]; then
  key="-h"
else
  key="$1"
fi
case $key in
    --cave)
        check_yumfile
        yum groupinstall awips2-cave -y 2>&1 | tee -a /tmp/awips-install.log
        ;;
    --server|--edex)
        server_prep
        yum groupinstall awips2-server -y 2>&1 | tee -a /tmp/awips-install.log
        ;;
    --ingest)
        server_prep
        yum groupinstall awips2-ingest -y 2>&1 | tee -a /tmp/awips-install.log
        ;;
    -h|--help)
        echo -e $usage
        exit
        ;;
esac

PATH=$PATH:/awips2/edex/bin/
exit
