#!/bin/bash


IOT_INTERNALID=helloid

while getopts s:t:i: option
do
        case "${option}"
        in      s) IOT_INTERNALID=${OPTARG};;
                t) IOT_DEVICE_TYPE=${OPTARG};;
				i) IOT_DEVICE_ID=${OPTARG};;
        esac
done

## DEFINE id devices array variable
declare -a devices=("dev1" "dev2" "dev3" "dev4" "dev5")
## DEFINE type devices array variable
declare -a typedevices=("g1" "g1" "g2" "g2" "g2")
## DEFINE metric tag array variable
declare -a tags=("availability" "load")
## DEFINE kpis array variable
## declare -a kpis=("avai.1" "load.1")

## eventdate is in local time
eventdate=`date +'%Y%m%d%H%M%S'`
## isoeventdate is in UTC time
isoeventdate=`date -u +'%Y-%m-%dT%H:%M:%S'`

# Generating JSON file
filename=$eventdate.json
# Using template file
cp simtempJson.txt $filename

# replace id and time
sed -i 's/#internalId#/"'$IOT_INTERNALID'"/g' "$filename"
sed -i 's/#timep#/"'$isoeventdate'"/g' "$filename"

# Generate kpis string.
#let c=0
#str="";
#for k in "${kpis[@]}"
#do
#   echo "$k"
#   if [ "$c" -gt 0 ]; then str=$str","
#   fi
#   # or do whatever with individual element of the array   
#   str=$str'"'"$k"'"'
#   let c=$c+1
#done
# Generate kpis string.
#kpis='"'"kpis"'"'":["$str"]"
#echo $kpis

# Replace kpis
sed -i 's/#kpis#/'$kpis'/g' "$filename"


let x=0
devstr="";
for IOT_DEVICE_ID in "${devices[@]}"
do
   echo "$IOT_DEVICE_ID"
   if [ "$x" -gt 0 ]; then devstr=$devstr","
   fi
   values="";
   sleep 1

   IOT_DEVICE_TYPE=${typedevices[x]}
   echo "$IOT_DEVICE_TYPE"
   
# GET availability value.
# generate random numbers between 0 and 1. 0 = device is not available. 1 = device is available
# REPLACE NEXT LINE WITH: availability=PlatformCustomScript.sh $IOT_DEVICE_ID $IOT_DEVICE_TYPE
availability=$((RANDOM%2))
values[0]=$availability;

# GET load value.
# generate random numbers between 0 and 1. 0 = device is not available. 1 = device is available

if [ $availability -eq 0 ]
then
	# if the device is not available, the load value is -1 that means that there is not information about it
	load=-1
else
	# if the device is available, the load value is a value between 0 and 100
	#REPLACE NEXT LINE WITH: load=PlatformCustomScript.sh $IOT_DEVICE_ID $IOT_DEVICE_TYPE
	load=$(((RANDOM%101)-1))
fi
values[1]=$load;

echo eventdate=$eventdate#IOT_DEVICE_ID=$IOT_DEVICE_ID#IOT_DEVICE_TYPE=$IOT_DEVICE_TYPE#AVAILABILITY=$availability#LOAD=$load




# Generate metrics string.
## now loop through the above tags array
let c=0
str="";
for i in "${tags[@]}"
do
   echo "$i"
   if [ "$c" -gt 0 ]; then str=$str","
   fi
   # or do whatever with individual element of the array   
   str=$str"{"'"'"tag"'"'":"'"'""$i""'"'","'"'"value"'"'":"${values[$c]}"}"
   let c=$c+1
done

metrics='"'"metrics"'"'":["$str"]"





# Generate device string.
## now loop through the above tags array
## isodevicedate is in UTC time
isodevicedate=`date -u +'%Y-%m-%dT%H:%M:%S'`
#devstr=$devstr"{"'"'"id"'"'":"'"'""$IOT_DEVICE_ID""'"'","'"'"timemetric"'"'":"'"'""$isodevicedate""'"'","$metrics"}" 

devstr=$devstr"{"'"'"id"'"'":"'"'""$IOT_DEVICE_ID""'"'","
devstr=$devstr'"'"type"'"'":"'"'""$IOT_DEVICE_TYPE""'"'","
devstr=$devstr'"'"timemetric"'"'":"'"'""$isodevicedate""'"'","$metrics"}" 

let x=$x+1
done

devices='"'"devices"'"'":["$devstr"]"

# Replace devices.
sed -i 's/#devices#/'$devices'/g' "$filename"




# show Json file		
cat $filename


#echo $sJson > $filename

curl -u user:password -H "Accept: application/json" -H "Content-type: application/json" -d@$filename localhost:8200/monitoring/$IOT_INTERNALID -X POST

# delete Json file
rm $filename

exit 0

