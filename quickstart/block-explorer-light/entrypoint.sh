#!/bin/bash


sed -i 's@Object({NODE_ENV:\"production\",BASE_URL:\"\/\"})@Object({NODE_URL: '"'$NODE_URL'"', CONNECTION_TYPE: '"'$CONNECTION_TYPE'"'})@g' js/*


serve -s -l 8080