param(
  [Parameter(Mandatory=$true, HelpMessage="The path to the lapack installation which include/cblas.h and dependent headers")]
  [string]$blasPath
)

. ../shared_windows.ps1

$jextract = find-tool("jextract")

& $jextract `
  -t blas `
  -I "$blasPath\include" `
  -l libcblas `
  --filter 'cblas.h' `
  -- `
  "$blasPath\include\cblas.h"
