param(
  [Parameter(Mandatory=$true, HelpMessage="The path to the tensorflow installation, which contains lib/tensorflow.dll")]
  [string]$tensorflowPath
)

. ../shared_windows.ps1

$java = find-tool("java")

& $java `
  -D"foreign.restricted=permit" `
  --add-modules jdk.incubator.foreign `
  -D"java.library.path=$tensorflowPath\lib" `
  TensorflowLoadSavedModel.java saved_mnist_model