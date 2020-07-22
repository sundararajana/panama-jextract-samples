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
  -J-Xmx2G `
  -J"-Djextract.log.cursors=true" `
  -J"-Djextract.debug=true" `
  -llibcurl `
  --filter 'curl' `
  -- `
  "$curlpath\include\curl\curl.h"
