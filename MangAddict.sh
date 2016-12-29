#!/bin/sh
appdir="" # leave empty if script is in same folder as .jar. End with a "/" if not!
nativesdir="bin/natives/windows&linux"
java -Djava.library.path=$appdir$nativesdir -jar $appdir"MangAddict.jar" "$@"