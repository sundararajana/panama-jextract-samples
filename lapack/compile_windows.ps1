param(
  [Parameter(Mandatory=$true, HelpMessage="The path to the lapack installation which include/lapacke.h and dependent headers")]
  [string]$lapackPath
)

. ../shared_windows.ps1

$jextract = find-tool("jextract")

& $jextract `
  -t lapack `
  -I "$lapackPath\include" `
  -l liblapacke `
  -J-Xmx2G `
  -J"-Djextract.log.cursors=true" `
  -J"-Djextract.debug=true" `
  --filter 'lapacke.h' `
  -- `
  "$lapackPath\include\lapacke.h"
