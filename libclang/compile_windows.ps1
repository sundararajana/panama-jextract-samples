param(
  [Parameter(Mandatory=$true, HelpMessage="The path to the llvm installation which include/clang-c")]
  [string]$clangPath
)

. ../shared_windows.ps1

$jextract = find-tool("jextract")

& $jextract `
  -t org.llvm.clang `
  -I "$clangPath\include" `
  -I "$clangPath\include\clang-c" `
  -l libclang `
  -J-Xmx2G `
  -J"-Djextract.log.cursors=true" `
  -J"-Djextract.debug=true" `
  -- `
  "$clangPath\include\clang-c\Index.h"
