#!/bin/sh

if [ $1 == 'true' ]; then
	search='^import org\.jtb\.alogcat\.R;$'
	replace='import org.jtb.alogcat.donate.R;'
else 
        search='^import org\.jtb\.alogcat\.donate\.R;$'
        replace='import org.jtb.alogcat.R;'
fi


find src -type f -name \*.java -print0 | xargs -0 sed -i ".tmp" "s!$search!$replace!g"
