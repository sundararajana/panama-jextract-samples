param(
  [Parameter(Mandatory=$true, HelpMessage="The path to the lapack installation which include/lapacke.h and dependent headers")]
  [string]$lapackPath
)

. ../shared_windows.ps1

$jextract = find-tool("jextract")

& $jextract `
  -I "$lapackPath\include" `
  --dump-includes 'includes_all.conf' `
  -- `
  "$lapackPath\include\lapacke.h"
  
filter_file 'includes_all.conf' 'lapacke.h' 'includes_filtered.conf'

& $jextract `
  -t lapack `
  -I "$lapackPath\include" `
  -l liblapacke `
  '@includes_filtered.conf' `
  -- `
  "$lapackPath\include\lapacke.h"
