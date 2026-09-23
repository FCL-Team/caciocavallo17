#!/bin/bash
set -e

mvn -B package --file pom.xml -DskipTests

rm -rf out
mkdir -p out
cp cacio-*/target/cacio-*-1.19.1-SNAPSHOT.jar out
mv out/cacio-agent-1.19.1-SNAPSHOT.jar out/cacio-agent.jar

# 下载专门的字节码工具
if [ ! -f "JavaDowngrader-Standalone-1.1.2.jar" ]; then
    wget -q https://github.com/RaphiMC/JavaDowngrader/releases/download/v1.1.2/JavaDowngrader-Standalone-1.1.2.jar
fi

for jarfile in out/cacio-*.jar; do
        echo "Downgrading: $(basename $jarfile)"
        java -jar JavaDowngrader-Standalone-1.1.2.jar -v 8 -i "$jarfile" -o "$jarfile.tmp"
        mv "$jarfile.tmp" "$jarfile"
done
