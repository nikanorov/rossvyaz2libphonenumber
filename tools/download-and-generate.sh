#!/bin/sh

url=https://rossvyaz.ru/data/DEF-9xx.csv
replace_file="replace_ru.csv"
output_file="DEF-9xx.csv"

echo "Downloading file from rossvyaz.ru..."
curl --silent ${url} | iconv -f windows-1251 -t utf-8 > ${output_file}
echo "Processing file..."
./rossvyaz2libphonenumber export ${output_file} ${replace_file}
echo "Removing temp file..."
rm ${output_file}
