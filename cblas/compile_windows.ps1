param(
  [Parameter(Mandatory=$true, HelpMessage="The path to the lapack installation which include/cblas.h and dependent headers")]
  [string]$blasPath
)

. ../shared_windows.ps1

$jextract = find-tool("jextract")

& $jextract `
  -I "$blasPath\include" `
  --dump-includes 'includes_all.conf' `
  -- `
  "$blasPath\include\cblas.h"
  
filter_file 'includes_all.conf' 'cblas.h' 'includes_filtered.conf'

& $jextract `
  -t blas `
  -I "$blasPath\include" `
  -l libcblas `
  '@includes_filtered.conf' `
  -- `
  "$blasPath\include\cblas.h"
