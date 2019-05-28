# rossvyaz2libphonenumber

Simple tool to convert phone number prefixes from the [Rossvyaz Numbering base](https://rossvyaz.ru/deyatelnost/resurs-numeracii/vypiska-iz-reestra-sistemy-i-plana-numeracii) to the [libphonenumber](https://github.com/googlei18n/libphonenumber) format.

## How to use
You could use included *DownloadAndGenerate.ps1* PowerShell script or download and process file manually.

To process manually:
1. Download fresh def file: https://rossvyaz.ru/data/DEF-9xx.csv
2. Convert it to the UTF-8.
3. Run the converter 
```
rossvyaz2libphonenumber export DEF-9xx.csv replace.csv
```

