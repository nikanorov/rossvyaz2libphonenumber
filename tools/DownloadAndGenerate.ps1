$url = "https://rossvyaz.ru/data/DEF-9xx.csv"
$output = "$PSScriptRoot\DEF-9xx.csv"
$outputUTF = "$PSScriptRoot\DEF-9xx-UTF.csv"
$replace = "$PSScriptRoot\replace_ru.csv"


Write-Host "Downloading file from rossvyaz.ru..."
Invoke-WebRequest -Uri $url -OutFile $output
Write-Host "Converting to UTF-8..."
Get-Content $output | Set-Content -Encoding utf8 $outputUTF
Write-Host "Processing file..."
&.\rossvyaz2libphonenumber.exe "export" $outputUTF $replace
Write-Host "Removing temp files..."
Remove-Item $output
Remove-Item $outputUTF
Write-Host "Done. Press any key to continue..."
[void][System.Console]::ReadKey($true)
