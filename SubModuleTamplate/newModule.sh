#!/bin/bash
# newModule.sh 模块名 包名
dirName=${1}
packageName=${2}
echo "dirName :" ${dirName} "packageName :" ${packageName}
if [ "$dirName" =  "" ];then
    echo "模块名不能为空"
    exit
fi
if [ "$packageName" = "" ]; then
    echo "包名不能为空"
    exit
fi
targetdir="../"${dirName}
if [ ! -d ${targetdir} ];then
  mkdir $targetdir
else
  echo "文件夹已经存在"
  exit
fi

echo "ext {packageName = \"${packageName}\"}" >>../${dirName}/build.gradle
echo "apply from: \"../SubModuleTamplate/sub_module.gradle\"" >>../${dirName}/build.gradle
echo "dependencies {implementation fileTree(dir: \"libs\", include: [\"*.jar\"]) }" >>../${dirName}/build.gradle
echo "include ':""${dirName}""'" >> ../settings.gradle
cd ..
pwd
$(pwd)/gradlew config
git add ./${dirName}/build.gradle
git add ./${dirName}/src/*
