#!/bin/sh
find $1 -name '*.*' -print0 | \
xargs -0 -I FILE \
sh -c 'ffmpeg -i "$1" -ac 1 -ar 22050 -f wav "${1%.*}_mono.wav"' -- FILE