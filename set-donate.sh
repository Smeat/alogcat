#!/bin/sh

if [ $1 == 'true' ]; then
	search='^import org.jtb.alogcat.R;$'
	replace='import org.jtb.alogcat.donate.R;'
else 
        search='^import org.jtb.alogcat.donate.R;$'
        replace='import org.jtb.alogcat.R;'
fi


find . -type f -name \*.java -print0 | xargs -0 sed -i "$search/$replace/g"
