#!/usr/bin/env bash
docker run -it --rm -p 1884:1883 --name hsweb-iot-cloud-haproxy \
 -v ${PWD}/haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg haproxy:1.7 \
 haproxy -f /usr/local/etc/haproxy/haproxy.cfg
