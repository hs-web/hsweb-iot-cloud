#!/usr/bin/env bash
if [ ! -d "bin" ]; then
    mkdir -p bin/
fi
cp -r data bin/
cp -r help.txt bin/
touch install.log
mvn clean compile -Dmaven.test.skip > install.log
install_result=$(cat install.log | tail -10)
if [[ $install_result =~ "BUILD SUCCESS" ]];then
      mvn assembly:assembly -Dmaven.test.skip > install.log
      install_result=$(cat install.log | tail -10)
      if [[ $install_result =~ "BUILD SUCCESS" ]];then
         cp target/mqtt-emulator-jar-with-dependencies.jar bin/mqtt-emulator.jar
         echo "构建成功"
         else
       cat install.log
      fi
   else
       cat install.log
fi
