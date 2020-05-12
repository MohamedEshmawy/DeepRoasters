from cx_Freeze import setup, Executable 
buildOptions = dict(include_files = ['core/']) #folder,relative path. Use tuple like in the single file to set a absolute path.

executables = [Executable("main.py", base = "Win32GUI")]

setup(name = "DeepTracker",options={"build_exe":{"packages":["torch","torchvision"]}},executables=executables)
