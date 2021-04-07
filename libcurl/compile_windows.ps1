param(
  [Parameter(Mandatory=$true, HelpMessage="The path to the lib curl installation")]
  [string]$curlpath
)

. ../shared_windows.ps1

$jextract = find-tool("jextract")

& $jextract `
  -t org.jextract `
  -I "$curlpath\include" `
  -I "$curlpath\include\curl" `
  -llibcurl `
  --filter 'curl' `
  -- `
  "$curlpath\include\curl\curl.h"
