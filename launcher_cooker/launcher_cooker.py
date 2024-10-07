import os
import shutil
from re import search

filename = "output/MusicService.smali"
os.makedirs(os.path.dirname(filename), exist_ok=True)
shutil.copy("MusicService.smali", "./output")

lookup = "sput-object v0, Lcom/fyt/car/MusicService;->album:Ljava/lang/String;"
substring = ".line"
with open(filename, "r") as f:
    lines = f.readlines()
    for k,line in enumerate(lines) :
        if lookup in line:
            if not search(substring, lines[k-3]): 
                lines.insert(k+2, "    invoke-virtual {p0}, Lcom/fyt/car/MusicService;->sendData()V" + '\n')
                lines.insert(k+3, '\n')

with open("additional_method.smali", "r") as f_add:
    linesAdd = f_add.readlines()

with open(filename, "w") as f:
    lines = "".join(lines)
    f.write(lines)
    f.write('\n')
    f.writelines(linesAdd)
    f.close()