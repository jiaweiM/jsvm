
Refactor from the java version of the LIBSVM.

https://github.com/cjlin1/libsvm

# SVMScale
Usage:
```cmd
SVMScale [options] dataFile 
```
|Option|Description|
|---|---|
|`-l lower`|x scaling lower limit (default -1)|
|`-u upper`|x scaling upper limit (default +1)|
|`-y yLower yUpper`|y scaling limits (default no y scaling)|
|`-s rangeFile`|save scaling parameters to rangeFile|
|`-r restoreFile`|restore scaling parameter from restoreFile|
|`-o outFile`|output scaling result to the outFile|

# SVMTrain
Usage:
```cmd
SVMTrain [options] training_set_file [model_file]
```

Options：
- `-s svm_type`

set SVM Type, default C-SVC.

|value|Type|
|---|---|
|0|C-SVC, multi-class classification|
|1|nu-SVC, multi-class classification|
|2|one-class SVM|
|3|epsilon-SVR (regression)|
|4|nu-SVM (regression)|

